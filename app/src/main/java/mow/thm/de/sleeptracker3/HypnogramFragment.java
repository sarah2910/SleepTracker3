package mow.thm.de.sleeptracker3;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HypnogramFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HypnogramFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private HypnogrammFragmentListener listener;

    PointsGraphSeries<DataPoint> xySeries;

    ArrayList<String> timeOfNumAwakeX = new ArrayList<>();

    private Button graphBtn;
    private Button printBtn;
    GraphView mScatterPlot;
    private ArrayList<XYValue> xyValueArray;

    public HypnogramFragment() {
        // Required empty public constructor
    }

    public interface HypnogrammFragmentListener {
        void onInputHypnogrammSent(ArrayList<String> timeOfNumAwakeX);

    }

    // TODO: Rename and change types and number of parameters
    public static HypnogramFragment newInstance(String param1, String param2) {
        HypnogramFragment fragment = new HypnogramFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            timeOfNumAwakeX = getArguments().getStringArrayList("TimeOfNumAwakeX");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_hypnogram, container, false);

        RecordingFragment recording = (RecordingFragment)getParentFragment();
        graphBtn = (Button)rootView.findViewById(R.id.graphBtn);
        printBtn = (Button)rootView.findViewById(R.id.printBtn);
        mScatterPlot = (GraphView)rootView.findViewById(R.id.testGraph);
        xyValueArray = new ArrayList<>();

        xySeries = new PointsGraphSeries<>();
        graphBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xySeries = new PointsGraphSeries<>();
                createScatterPlot();
            }
        });

        printBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i < timeOfNumAwakeX.size(); i++) {
                    System.out.println("Datum " + i + ":" + timeOfNumAwakeX.get(i));
                }

            }
        });




        return rootView;
    }








    private void toastMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void createScatterPlot() {
        Log.d(getTag(), "SOOOOO ... der plot wird gemacht hehe");

        String newTimeOfNumAwakeX[] = new String[timeOfNumAwakeX.size()];

        for(int i = 0; i < timeOfNumAwakeX.size(); i++) {
            try {
                xySeries.appendData(new DataPoint(i,100), true, 1000);
                newTimeOfNumAwakeX[i] = timeOfNumAwakeX.get(i);
            } catch (IllegalArgumentException e) {
                Log.d(getTag(), "createScatterPlot: IllegalArgumentException: " + e.getMessage());
            }
        }




        //set some properties
        xySeries.setShape(PointsGraphSeries.Shape.RECTANGLE);
        xySeries.setColor(Color.parseColor("#201B52"));
        xySeries.setSize(20f);

        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(mScatterPlot);
        staticLabelsFormatter.setHorizontalLabels(newTimeOfNumAwakeX);
        mScatterPlot.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

        //set Scrollable and Scaleable
        mScatterPlot.getViewport().setScalable(true);
        mScatterPlot.getViewport().setScalableY(true);
        mScatterPlot.getViewport().setScrollable(true);
        mScatterPlot.getViewport().setScrollableY(true);

        //set manual x bounds
        mScatterPlot.getViewport().setYAxisBoundsManual(true);
        mScatterPlot.getViewport().setMaxY(150);
        mScatterPlot.getViewport().setMinY(-150);

        //set manual y bounds
        mScatterPlot.getViewport().setXAxisBoundsManual(true);
        mScatterPlot.getViewport().setMaxX(150);
        mScatterPlot.getViewport().setMinX(-150);

        mScatterPlot.getViewport().setScalable(true);  // activate horizontal zooming and scrolling
        mScatterPlot.getViewport().setScrollable(true);  // activate horizontal scrolling
        mScatterPlot.getViewport().setScalableY(true);  // activate horizontal and vertical zooming and scrolling
        mScatterPlot.getViewport().setScrollableY(true);  // activate vertical scrolling

        mScatterPlot.addSeries(xySeries);
    }

    private ArrayList<XYValue> sortArray(ArrayList<XYValue> array) {
        int factor = Integer.parseInt(String.valueOf(Math.round(Math.pow(array.size(),2))));
        int m = array.size() - 1;
        int count = 0;

        while (true) {
            m--;
            if (m <= 0) {
                m = array.size() - 1;
            }

            try {
                //print out the y entrys so we know what the order looks like
                //Log.d(TAG, "sortArray: Order:");
                //for(int n = 0;n < array.size();n++){
                //Log.d(TAG, "sortArray: " + array.get(n).getY());
                //}
                double tempY = array.get(m - 1).getY();
                double tempX = array.get(m - 1).getX();
                if (tempX > array.get(m).getX()) {
                    array.get(m - 1).setY(array.get(m).getY());
                    array.get(m).setY(tempY);
                    array.get(m - 1).setX(array.get(m).getX());
                    array.get(m).setX(tempX);
                } else if (tempX == array.get(m).getX()) {
                    count++;
                } else if (array.get(m).getX() > array.get(m - 1).getX()) {
                    count++;
                }
                //break when factorial is done
                if (count == factor) {
                    break;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("HILFE ARRAY OUT OF BOUNDS!");
                break;
            }
        }
        return array;
    }



}