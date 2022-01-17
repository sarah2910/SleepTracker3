package mow.thm.de.sleeptracker3;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

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

    private String textStartingTime;
    private String textEndingTime;
    private String durationHrs;
    private String textDurationHrsAvg;
    private String allText;

    ArrayList<String> avgTimeList;

    private float minSleep = 8;
    Boolean moreThanAvg; // ob User mehr oder weniger als durchschnitt geschlafen hat
    Boolean moreThanMinSleep; // ob User mehr oder weniger als festgelegter Mindestschlaf geschlafen hat

    float hoursOfSleep;

    private Button submitEvaluationBtn;
    private Button minSleepBtn;

    String userChild;

    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReferenceMovementTime;
    DatabaseReference databaseReferenceHistory;

    DatabaseReference historyUser;
    DatabaseReference movementTimeUser;

    DatabaseReference historyAverage;

    DatabaseReference startingTime;
    DatabaseReference endingTime;

    //TODO: Analyse:
    int numAwake = 0;
    ArrayList<String> timeOfNumAwake = new ArrayList<>();

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

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        assert currentFirebaseUser != null;
        userChild = currentFirebaseUser.getUid()+"";

        databaseReferenceMovementTime = database.getReference("MovementTime");
        databaseReferenceHistory = database.getReference("History");

        movementTimeUser = databaseReferenceMovementTime.child(userChild);

        startingTime = movementTimeUser.child("startingTime");
        endingTime = movementTimeUser.child("endingTime");

        historyUser = databaseReferenceHistory.child(userChild);
        historyAverage = historyUser.child("Average");

        timeOfNumAwake.add("");


        View rootView = inflater.inflate(R.layout.fragment_evaluation, container, false);

        TextView textViewStartingTime = (TextView)rootView.findViewById(R.id.textStartingTime);
        TextView textViewEndingTime = (TextView)rootView.findViewById(R.id.textEndingTime);
        TextView textViewDurationTime = (TextView)rootView.findViewById(R.id.textDurationHrs);
        TextView textViewMinSleep = (TextView)rootView.findViewById(R.id.textMinSleep);
//        TextView textViewDurationTimeAvg = (TextView)rootView.findViewById(R.id.textDurationAvg);

        submitEvaluationBtn = (Button)rootView.findViewById(R.id.submitEvaluation);
        minSleepBtn = (Button)rootView.findViewById(R.id.minSleep); // Recommended Sleep (8h) durch Userinput ändern

        minSleepBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Change Minimum of Sleep");

                final EditText input = new EditText(getActivity());
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                builder.setView(input);

                builder.setPositiveButton(Html.fromHtml("<font color='#FFFFFF'>OK</font>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String i = input.getText().toString();

                        if(i.matches("") || Float.parseFloat(i)<0.0) {
                            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Error! Please Enter a valid number!", Toast.LENGTH_LONG).show();
                        } else {
                            minSleep = Float.parseFloat(i);
                            historyUser.child("minSleep").setValue(minSleep + "");
                            moreThanMinSleep = (hoursOfSleep > minSleep);
                            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "minSleep changed!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.setNegativeButton(Html.fromHtml("<font color='#FFFFFF'>CANCEL</font>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        submitEvaluationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(textStartingTime != null && textEndingTime != null && !Float.isNaN(hoursOfSleep)) {
                    addEvaluationToFirebase();
                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Submitted!", Toast.LENGTH_LONG).show();
                } else {
                    System.out.println("startingTime: " + startingTime + " endingTime: " + endingTime + " durationHrs: " + durationHrs);
                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Error! Please record Sleeping Time!", Toast.LENGTH_LONG).show();
                }
            }
        });

        historyUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                avgTimeList = new ArrayList<>();

                for(DataSnapshot dsp : snapshot.getChildren()) {
                    Object durationChild = dsp.child("SleepAvg").child("duration").getValue();
                    if(durationChild != null) {
                        avgTimeList.add(durationChild.toString());
                        System.out.println("added to avgTimeList! ->" + durationChild.toString());
                    }
                }
                if(avgTimeList.size() > 0) {
                    String newStr = calcAvgHrs();
                    historyAverage.setValue(newStr);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        historyUser.child("minSleep").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                moreThanMinSleep = (hoursOfSleep>minSleep);

                String min = snapshot.getValue(String.class);
                if(min != null) {
                    minSleep=Float.parseFloat(min);
                    textViewMinSleep.setText("MinSleep: " + min + " Hours");
                } else {
                    textViewMinSleep.setText("MinSleep: EMPTY");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        historyAverage.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String duration = snapshot.getValue(String.class);
                System.out.println("AVG: " + duration);
                textDurationHrsAvg = duration;

                if(textDurationHrsAvg != null && !textDurationHrsAvg.isEmpty()) {
                    String text = "Average Sleeping Time: " + textDurationHrsAvg + " hours!";

//                    textViewDurationTimeAvg.setText(text);
                } else {
//                    textViewDurationTimeAvg.setText("Average Sleeping Time: EMPTY");
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
                } else {
                    textViewEndingTime.setText("Ending Time: EMPTY");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        historyUser.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (textEndingTime != null) {
                    textViewEndingTime.setText("Ending Time: "+textEndingTime);

                    if(textStartingTime != null && textEndingTime != null) {
                        String start = (String) textStartingTime;
                        String end = (String) textEndingTime;

                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                        try {
                            Date d1 = sdf.parse(start);
                            Date d2 = sdf.parse(end);

                            long diff_ms = d2.getTime() - d1.getTime();
                            long diff_s = (diff_ms/1000)%60;
                            long diff_min = (diff_ms / (1000 * 60)) % 60;
                            long diff_h = (diff_ms / (1000 * 60 * 60)) % 24;

                            hoursOfSleep = (float) ((float)diff_ms / (1000 * 60))/60;

                            if(textDurationHrsAvg != null) {
                                moreThanAvg = hoursOfSleep > Float.parseFloat(textDurationHrsAvg);
                            }

                            moreThanMinSleep = (hoursOfSleep>minSleep);

                            String text = "You slept " + diff_h + " Hours, " + diff_min + " Minutes, " + diff_s + " Seconds!";

                            if(moreThanAvg != null && moreThanMinSleep != null) {
                                text+=" \n\nThis is ";
                                text+= ((moreThanAvg) ? "more" : "less");
                                text += " than your average Sleep ("+ textDurationHrsAvg + "h) and\n";
                                text+= ((moreThanMinSleep) ? "more" : "less");
                                text+= " than your minimum Sleep.\n\n";
                                text+= ((moreThanMinSleep) ? "" : ":(");
                            }
                            textViewDurationTime.setText(text);

                            if(moreThanMinSleep) {
                                ImageView imgView = (ImageView) rootView.findViewById(R.id.happy);
                                if(imgView != null)
                                    imgView.setVisibility(View.VISIBLE);
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    public void addEvaluationToFirebase() {

        String dateChild = textStartingTime.substring(0,10); // Datum ohne Uhrzeit
        durationHrs = hoursOfSleep+"";
        History history = new History(textStartingTime, textEndingTime, durationHrs); //TODO

        historyUser.child(dateChild).child("SleepAvg").setValue(history);
    }

    public String calcAvgHrs() {

        float res = 0;

        if(avgTimeList.isEmpty()) {
            return "";

        } else {
            float tmp = 0;
            for(int i=0; i<avgTimeList.size(); i++) {
                tmp = Float.parseFloat(avgTimeList.get(i));
                res += tmp;
            }
            res /= avgTimeList.size();
        }

        return res+"";
    }
}