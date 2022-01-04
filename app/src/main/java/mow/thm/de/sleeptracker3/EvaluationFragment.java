package mow.thm.de.sleeptracker3;

import android.hardware.Sensor;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
        TextView textViewDurationTime = (TextView)rootView.findViewById(R.id.textDuration);

//        btnShowData.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {

                final FirebaseDatabase database = FirebaseDatabase.getInstance();

                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
                assert currentFirebaseUser != null;
                String userChild = currentFirebaseUser.getUid()+"";

                DatabaseReference databaseReference = database.getReference("MovementTime");
                DatabaseReference startingTime = databaseReference.child(userChild).child("startingTime");
                DatabaseReference endingTime = databaseReference.child(userChild).child("endingTime");

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
}