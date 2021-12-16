package mow.thm.de.sleeptracker3;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import com.google.firebase.auth.FirebaseUser;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecordingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordingFragment extends Fragment implements SensorEventListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button startbtn, stopbtn, startSensorBtn, stopSensorBtn;
    private MediaRecorder recorder;
    private Sensor mySensor;
    private SensorManager SM;
    Timer timer;
    long start;
    long end;
    long delta;
    ArrayList<Float> movementDataX;
    ArrayList<Float> movementDataY;
    ArrayList<Float> movementDataZ;
    private static final String LOG_TAG = "AudioRecording";
    private static String mFileName = null;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference databaseReferenceTime;
    MovementInfo movementInfo;
    MovementTime movementTime;


    public RecordingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecordingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecordingFragment newInstance(String param1, String param2) {
        RecordingFragment fragment = new RecordingFragment();
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

        SM = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        movementDataX = new ArrayList<Float>();
        movementDataY = new ArrayList<Float>();
        movementDataZ = new ArrayList<Float>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_recording, container, false);

        startbtn = (Button)rootView.findViewById(R.id.btnRecord);
        startSensorBtn = (Button)rootView.findViewById(R.id.btnSensorRecord);
        stopbtn = (Button)rootView.findViewById(R.id.btnStop);
        stopSensorBtn = (Button)rootView.findViewById(R.id.btnSensorStop);
        stopbtn.setEnabled(false);
        stopSensorBtn.setEnabled(false);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyFolder/";
        File dir = new File(path);
        if(!dir.exists())
            dir.mkdirs();
        String myfile = path + "filename" + ".3gp";

        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CheckPermissions()) {
                    Log.e(LOG_TAG, "permissions passen");
                    stopbtn.setEnabled(true);
                    startbtn.setEnabled(false);
                    recorder = new MediaRecorder();
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    recorder.setOutputFile(myfile);

                    try {
                        recorder.prepare();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "prepare() failed");
                        System.out.println(""+e);
                    }

                    recorder.start();
                    Toast.makeText(getActivity().getApplicationContext(), "Recording Started", Toast.LENGTH_LONG).show();
                } else {
                    RequestPermissions();
                }

            }
        });

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("MovementInfo");
        databaseReferenceTime = firebaseDatabase.getReference("MovementTime");
        movementInfo = new MovementInfo();
        movementTime = new MovementTime();

        // sendDatabtn = startSensorBtn --> Daten sollen in DB gespeichert werden
        startSensorBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

                start = System.currentTimeMillis();

                movementDataX.clear();
                movementDataY.clear();
                movementDataZ.clear();

                timer = new Timer();

                stopSensorBtn.setEnabled(true);
                startSensorBtn.setEnabled(false);
                createSensor();

                //TODO: if(...)
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"); // Geht nur ab API Level 26!
                String startString = now.format(dateTimeFormatter);
                addStartTimeDataToFirebase(startString);

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("TEEEEEESSSSST");

                        end = System.currentTimeMillis();
                        delta = end - start;

                        float x = 0;
                        float y = 0;
                        float z = 0;

                        onPause();

                        for (int i = 0; i < movementDataX.size(); i++) {
                            x += movementDataX.get(i);
                        }

                        for (int i = 0; i < movementDataY.size(); i++) {
                            y += movementDataY.get(i);
                        }

                        for (int i = 0; i < movementDataZ.size(); i++) {
                            z += movementDataZ.get(i);
                        }

                        x = x/movementDataX.size();
                        y = y/movementDataY.size();
                        z = z/movementDataZ.size();

                        // In Firebase speichern
                        if(x>0 && y>0 && z>0 /*&& delta>0*/)
                            addDataToFirebase(x, y, z/*, delta*/);


                        System.out.println("Im Mittel alle 3 Sekunden auf der X Achse: " + x);
                        System.out.println("Im Mittel alle 3 Sekunden auf der Y Achse: " + y);
                        System.out.println("Im Mittel alle 3 Sekunden auf der Z Achse: " + z);
                        System.out.println("Abgelaufene Zeit in ms: " + delta/1000);

                        movementDataX.clear();
                        movementDataY.clear();
                        movementDataZ.clear();

                        createSensor();
                    }
                }, 0, 3000);

            }
        });

        stopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopbtn.setEnabled(false);
                startbtn.setEnabled(true);
                recorder.stop();
                recorder.release();
                recorder = null;
            }
        });

        stopSensorBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                timer.cancel();
                stopSensorBtn.setEnabled(false);
                startSensorBtn.setEnabled(true);

                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"); // Geht nur ab API Level 26!
                String endString = now.format(dateTimeFormatter);
                addEndTimeDataToFirebase(endString);

                onPause();
            }
        });

        return rootView;
    }

    private void addDataToFirebase(float x, float y, float z/*, long delta*/) {
        movementInfo.setX(x);
        movementInfo.setY(y);
        movementInfo.setZ(z);
        //movementInfo.setDelta(delta);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
                assert currentFirebaseUser != null;
                String userChild = currentFirebaseUser.getUid()+"";

                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"); // Geht nur ab API Level 26!
                String dateChild = now.format(dateTimeFormatter);

                if(!userChild.isEmpty())
                {
                    databaseReference.child(userChild+"").child(dateChild).setValue(movementInfo);
                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "data added", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "userChild was empty!", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Fail to add data", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void addStartTimeDataToFirebase(String startingTime) {

        movementTime.setStartingTime(startingTime);

        databaseReferenceTime.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
                assert currentFirebaseUser != null;
                String userChild = currentFirebaseUser.getUid()+"";

                if(!userChild.isEmpty())
                {
                    databaseReferenceTime.child(userChild+"").setValue(movementTime);
                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "data added", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "userChild was empty!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Fail to add data", Toast.LENGTH_SHORT).show();
            }
        });
    }
     public void addEndTimeDataToFirebase(String endingTime) {

            movementTime.setEndingTime(endingTime);

            databaseReferenceTime.addValueEventListener(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
                    assert currentFirebaseUser != null;
                    String userChild = currentFirebaseUser.getUid()+"";

                    if(!userChild.isEmpty())
                    {
                        databaseReferenceTime.child(userChild+"").setValue(movementTime);
                        Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "data added", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "userChild was empty!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Fail to add data", Toast.LENGTH_SHORT).show();
                }
            });
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length> 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] ==  PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        Toast.makeText(getActivity().getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(),"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean CheckPermissions() {
        int result = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        requestPermissions(new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        movementDataX.add(event.values[0]);
        movementDataY.add(event.values[1]);
        movementDataZ.add(event.values[2]);

        /*
        System.out.println("X: " + event.values[0]);
        System.out.println("Y: " + event.values[1]);
        System.out.println("Z: " + event.values[2]);
        */
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Nicht genutzt
    }

    public void createSensor() {
        SM.registerListener(this, mySensor,  10000000, 10000000);
    }

    public void onPause() {
        super.onPause();
        SM.unregisterListener(this);
    }

    public void averageMovement() {

    }


}