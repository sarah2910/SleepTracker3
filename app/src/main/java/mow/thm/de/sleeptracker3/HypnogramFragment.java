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

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
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

    PointsGraphSeries<DataPoint> xySeries;

    private Button graphBtn;
    private Button printBtn;

    private float N = 0.0f;
    private float AnzahlAwake = 0.0f;
    private float AnzahlLight = 0.0f;
    private float AnzahlMedium = 0.0f;
    private float AnteilAwake = 0.0f;
    private float AnteilLight = 0.0f;
    private float AnteilMedium = 0.0f;
    private float[] Schlafanteile = {20.0f, 40.0f, 0.0f};
    private String[] Schlafkategorien = {"Wachzustand", "leichter Schlaf", "tiefer Schlaf"};
    PieChart pieChart;

    //GraphView mScatterPlot;
    //private ArrayList<XYValue> xyValueArray;

    public HypnogramFragment() {
        // Required empty public constructor
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

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_hypnogram, container, false);

        graphBtn = (Button)rootView.findViewById(R.id.chartBtn);
        printBtn = (Button)rootView.findViewById(R.id.printBtn);
        pieChart = (PieChart)rootView.findViewById(R.id.pieChart);
        Description description = pieChart.getDescription();
        //mScatterPlot = (GraphView)rootView.findViewById(R.id.testGraph);
        //xyValueArray = new ArrayList<>();
        //timeOfNumAwakeAll = RecordingFragment.timeOfNumAwakeAll;

        description.setText("Anteile von leichtem, mittlerem und tiefem Schlaf");
        pieChart.setRotationEnabled(true);
        pieChart.setHoleRadius(25.f);
        pieChart.setCenterText("Schlafanteile");
        pieChart.setCenterTextSize(10);



        //xySeries = new PointsGraphSeries<>();
        graphBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                xySeries = new PointsGraphSeries<>();
                createScatterPlot();
                */

                if (RecordingFragment.timeOfNumAwakeAll2.size() != 0 && RecordingFragment.timeOfNumAwakeAll.size() != 0) {
                    System.out.println("TEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEST");
                    AnzahlAwake = RecordingFragment.timeOfNumAwakeAll.size();
                    AnzahlLight = RecordingFragment.timeOfNumAwakeAll2.size();
                    AnzahlMedium = 0.0f;

                    System.out.println("AnzahlAwake " + AnzahlAwake);
                    System.out.println("AnzahlLight " + AnzahlLight);

                    N = AnzahlAwake + AnzahlLight + AnteilMedium;

                    AnteilAwake = AnzahlAwake / N * 100;
                    AnteilLight = AnzahlLight / N * 100;
                    AnteilMedium = AnzahlMedium / N * 100;

                    Schlafanteile[0] = AnteilAwake;
                    Schlafanteile[1] = AnteilLight;
                    Schlafanteile[2] = AnteilMedium;

                    addDataSet();
                } else {
                    toastMessage("Noch keine aktuellen Daten vorhanden");
                }



            }
        });

        printBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                for(int i = 0; i < timeOfNumAwakeAll.size(); i++) {
                    System.out.println("Datum " + i + ":" + timeOfNumAwakeAll.get(i));
                }
                */
            }
        });




        return rootView;
    }


    private void addDataSet() {
        ArrayList<PieEntry> EinträgeSchlafanteile = new ArrayList<>();
        ArrayList<String> EinträgeSchlafkategorien = new ArrayList<>();

        for(int i = 0; i < Schlafanteile.length; i++) {
            EinträgeSchlafanteile.add(new PieEntry(Schlafanteile[i], i));
        }

        for(int i = 0; i < Schlafkategorien.length; i++) {
            EinträgeSchlafkategorien.add(Schlafkategorien[i]);
        }

        PieDataSet Datensatz = new PieDataSet(EinträgeSchlafanteile, "Schlafanteile");
        Datensatz.setSliceSpace(2);
        Datensatz.setValueTextSize(12);

        ArrayList<Integer> farben = new ArrayList<>();
        farben.add(Color.RED);
        farben.add(Color.YELLOW);
        farben.add(Color.GREEN);

        Datensatz.setColors(farben);

        Legend Legende = pieChart.getLegend();
        Legende.setForm(Legend.LegendForm.CIRCLE);
        Legende.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        Legende.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        Legende.setTextColor(Color.WHITE);

        PieData Daten = new PieData(Datensatz);
        pieChart.setData(Daten);
        pieChart.invalidate();


    }


    private void toastMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    /*
    private void createScatterPlot() {
        Log.d(getTag(), "SOOOOO ... der plot wird gemacht hehe");

        String newTimeOfNumAwakeX[] = new String[timeOfNumAwakeAll.size()];

        for(int i = 0; i < timeOfNumAwakeAll.size(); i++) {
            try {
                xySeries.appendData(new DataPoint(i,100), true, 1000);
                newTimeOfNumAwakeX[i] = timeOfNumAwakeAll.get(i);
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
    */




}