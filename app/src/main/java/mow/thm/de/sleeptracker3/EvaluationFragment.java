package mow.thm.de.sleeptracker3;

import android.hardware.Sensor;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EvaluationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EvaluationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

//    private Button btnShowData;
    private String textStartingTime;
    private String textEndingTime;
    private String durationHrs;
    private String textDurationHrsAvg;
    private String dateChild = "";

    ArrayList<String> avgTimeList;

    float hoursOfSleep;

    private Button submitEvaluationBtn;

    //TODO:
    DatabaseReference databaseReference;
    DatabaseReference startingTime;
    DatabaseReference endingTime;
    DatabaseReference databaseReferenceH;
    DatabaseReference databaseReferenceHistory;
    DatabaseReference databaseReferenceAverage;


    public EvaluationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EvaluationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EvaluationFragment newInstance(String param1, String param2) {
        EvaluationFragment fragment = new EvaluationFragment();
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_evaluation, container, false);

//        btnShowData = (Button)rootView.findViewById(R.id.btnShowData);
        TextView textViewStartingTime = (TextView)rootView.findViewById(R.id.textStartingTime);
        TextView textViewEndingTime = (TextView)rootView.findViewById(R.id.textEndingTime);
        TextView textViewDurationTime = (TextView)rootView.findViewById(R.id.textDurationHrs);
        TextView textViewDurationTimeAvg = (TextView)rootView.findViewById(R.id.textDurationAvg);

        submitEvaluationBtn = (Button)rootView.findViewById(R.id.submitEvaluation);


        submitEvaluationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(textStartingTime != null && textEndingTime != null) {
                    addEvaluationToFirebase();
                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Submitted!", Toast.LENGTH_LONG).show();
                } else {
                    System.out.println("startingTime: " + startingTime + " endingTime: " + endingTime + " durationHrs: " + durationHrs);
                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Error! Please record Sleeping Time!", Toast.LENGTH_LONG).show();
                }

            }
        });
//        btnShowData.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        assert currentFirebaseUser != null;
        String userChild = currentFirebaseUser.getUid()+"";

        databaseReference = database.getReference("MovementTime");
        databaseReferenceH = database.getReference("History");

        startingTime = databaseReference.child(userChild).child("startingTime");
        endingTime = databaseReference.child(userChild).child("endingTime");

        databaseReferenceHistory = databaseReferenceH.child(userChild).child("Average");

        DatabaseReference databaseReferenceUser = databaseReferenceH.child(userChild);

        databaseReferenceUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                avgTimeList = new ArrayList<>();

                for(DataSnapshot dsp : snapshot.getChildren()) {

                    Object durationChild = dsp.child("duration").getValue();
                    if(durationChild != null) {
                        Object userVal = dsp.getValue();
//                        System.out.println("!!!!TEST!!!!: " + userVal);
//                        System.out.println("2. !!!!TEST!!!!: " + dsp.child("duration").getValue());
                        avgTimeList.add(durationChild.toString());
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReferenceHistory.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String duration = snapshot.getValue(String.class);
                System.out.println("AVG: " + duration);
                textDurationHrsAvg = duration;

                if(textDurationHrsAvg != null) {
                    textViewDurationTimeAvg.setText("Average Sleeping Time: " + textDurationHrsAvg + " hours!");
                } else {
                    textViewDurationTimeAvg.setText("Average Sleeping Time: EMPTY");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        startingTime.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String start = snapshot.getValue(String.class);
                System.out.println("start: " + start);
                textStartingTime = start;

                if (textStartingTime != null) {
                    textViewStartingTime.setText("Starting Time: "+textStartingTime);
                } else {
                    textViewStartingTime.setText("Starting Time: EMPTY");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
            }
        });
        endingTime.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String end = snapshot.getValue(String.class);
                System.out.println("end: " + end);
                textEndingTime = end;

                if (textEndingTime != null) {
                    textViewEndingTime.setText("Ending Time: "+textEndingTime);

                    //TODO:
                    if(textStartingTime != null) {
                        String start = (String) textStartingTime;

                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                        try {
                            Date d1 = sdf.parse(start);
                            Date d2 = sdf.parse(end);

                            long diff_ms = d2.getTime() - d1.getTime();
                            long diff_s = (diff_ms/1000)%60;
                            long diff_min = (diff_ms / (1000 * 60)) % 60;
                            long diff_h = (diff_ms / (1000 * 60 * 60)) % 24;
                            textViewDurationTime.setText("You slept " + diff_h + " Hours, " + diff_min + " Minutes, " + diff_s + " Seconds!");

                            hoursOfSleep = (float) ((float)diff_ms / (1000 * 60))/60;

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }


                } else {
                    textViewEndingTime.setText("Ending Time: EMPTY");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
            }
        });


//                if (textStartingTime != null) {
//                    textViewStartingTime.setText("Starting Time: "+textStartingTime);
//                } else {
//                    textViewStartingTime.setText("Starting Time: EMPTY");
//                }

//                if (textEndingTime != null) {
//                    textViewEndingTime.setText("Ending Time: "+textEndingTime);
//                } else {
//                    textViewEndingTime.setText("Ending Time: EMPTY");
//                }
//            }
//        });
        return rootView;
    }

    public void addEvaluationToFirebase() {
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        assert currentFirebaseUser != null;
        String userChild = currentFirebaseUser.getUid()+"";


        dateChild = textStartingTime.substring(0,10); // Datum ohne Uhrzeit

        durationHrs = hoursOfSleep+"";
        String durationHrsAvg = calcAvgHrs();

        History history = new History(textStartingTime, textEndingTime, durationHrs);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReferenceHistory = firebaseDatabase.getReference("History");

        databaseReferenceHistory.child(userChild+"").child("Average").setValue(durationHrsAvg);
        databaseReferenceHistory.child(userChild+"").child(dateChild).setValue(history);

    }

    public String calcAvgHrs() {

        float res = 0;

        if(avgTimeList.isEmpty()) {
            return "";

        } else {
            float tmp = 0;
            for(int i=0; i<avgTimeList.size(); i++) {
                tmp = Float.parseFloat(avgTimeList.get(i));
//                System.out.println("avgTimeList: "+avgTimeList.get(i)+" - tmp: " + tmp);
                res += tmp;
            }
            res /= avgTimeList.size();
        }

//        return durationHrs+"";
        return res+"";
    }
}