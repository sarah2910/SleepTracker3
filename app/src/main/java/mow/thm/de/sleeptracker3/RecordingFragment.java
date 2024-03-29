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
import android.os.PowerManager;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
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

    private RecordingFragmentListener listener;

    private Button startSensorBtn, stopSensorBtn;
    private Sensor mySensor;
    private SensorManager SM;
    Timer timer;
    PowerManager mgr;
    PowerManager.WakeLock wakeLock;
    long start;
    long end;
    long delta;

//    float minX=100, minY=100, minZ=100;
//    float maxX=-100, maxY=-100, maxZ=-100;
//    float avgX=0, avgY=0, avgZ=0;
//    float allX=0;
//    int i=1;

    // Wachzeiten:
    double peakDiff = 0.15; // Differenz zu Durchschnitt, ab dem Peak erkannt wird

    double awakeDur = 100; // Wie lange man ca zum Einschlafen braucht, hier: 5 Minuten --> Umgerechnet in Uhrzeiten (alle 3 Sekunden) --> 100x 3 Sekunden = 300 Sekunden = 5 Minuten
    double durTimesAwake = awakeDur*3*1000; // Zeit, die man zum Einschlafen braucht, in ms
    Boolean isAwake = false;

    ArrayList<Float> listX = new ArrayList<>(); // Hier sollen die letzten ~10 Werte gespeichert werden
    ArrayList<Float> listY = new ArrayList<>();
    ArrayList<Float> listZ = new ArrayList<>();

    ArrayList<String> timeOfNumAwakeX = new ArrayList<>();
    ArrayList<String> timeOfNumAwakeY = new ArrayList<>();
    ArrayList<String> timeOfNumAwakeZ = new ArrayList<>();
    ArrayList<String> timeOfNumAwakeAll = new ArrayList<>();

    int timesAwakeX = 0;
    int timesAwakeY = 0;
    int timesAwakeZ = 0;
    int timesAwakeAll = 0;

    Date tmpDateX;
    Date tmpDateY;
    Date tmpDateZ;
    Date tmpDateLast;

    // Leichter Schlaf:
    double peakDiff2 = 0.025; // erkennt auch kleinere Differenzen

    ArrayList<String> timeOfNumAwakeX2 = new ArrayList<>();
    ArrayList<String> timeOfNumAwakeY2 = new ArrayList<>();
    ArrayList<String> timeOfNumAwakeZ2 = new ArrayList<>();
    ArrayList<String> timeOfNumAwakeAll2 = new ArrayList<>();

    int timesAwakeX2 = 0;
    int timesAwakeY2 = 0;
    int timesAwakeZ2 = 0;
    int timesAwakeAll2 = 0;

    Date tmpDateX2;
    Date tmpDateY2;
    Date tmpDateZ2;
    Date tmpDateLast2;

//    int durTimesAwake = 300000; // Wie lange gewartet wird, bis Uhrzeiten wieder als "Wach" gespeichert werden: in ms! --> Hier: 5 Minuten


    String textStartingTime;

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

    FirebaseDatabase database;
    DatabaseReference databaseReferenceHistory;
    DatabaseReference databaseReferenceMovementTime;
    DatabaseReference historyUser;
    DatabaseReference movementTimeUser;
    DatabaseReference startingTime;

    private boolean recording = false;

    public interface RecordingFragmentListener {
        void onInputRecordingSent(ArrayList<String> timeOfNumAwakeX);

    }

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
        if (savedInstanceState == null) {
            mgr = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);
            wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag");

            SM = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            movementDataX = new ArrayList<Float>();
            movementDataY = new ArrayList<Float>();
            movementDataZ = new ArrayList<Float>();
        } else {
           // System.out.println("HALLLLLLLLLLLLLLLLLLLLOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO " + savedInstanceState.getBoolean("startSensorBtnState"));
           // startSensorBtn.setEnabled(savedInstanceState.getBoolean("startSensorBtnState"));
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_recording, container, false);

        startSensorBtn = (Button)rootView.findViewById(R.id.btnSensorRecord);
        stopSensorBtn = (Button)rootView.findViewById(R.id.btnSensorStop);
        stopSensorBtn.setEnabled(false);

        database = FirebaseDatabase.getInstance();
        databaseReferenceHistory = database.getReference("History");
        databaseReferenceMovementTime = database.getReference("MovementTime");
        historyUser = databaseReferenceHistory.child(FirebaseAuth.getInstance().getCurrentUser().getUid()+"");
        movementTimeUser = databaseReferenceMovementTime.child(FirebaseAuth.getInstance().getCurrentUser().getUid()+"");
        startingTime = movementTimeUser.child("startingTime");


        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyFolder/";
        File dir = new File(path);
        if(!dir.exists())
            dir.mkdirs();
        String myfile = path + "filename" + ".3gp";


        startingTime.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String start = snapshot.getValue(String.class);
                System.out.println("start: " + start);
                textStartingTime = start;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

                wakeLock.acquire();

                recording = true;

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
                        if(!Float.isNaN(x) && !Float.isNaN(y) && !Float.isNaN(z))
                        {
                            addDataToFirebase(x, y, z/*, delta*/);
                            System.out.println("ADD DATA TO FIREBASE");

//                            minX = (Math.min(x, minX));
//                            maxX = (Math.max(x, maxX));
//                            avgX = (allX+x)/i; // bringt nicht viel :)

                            listX.add(x);
                            listY.add(y);
                            listZ.add(z);

                            if(listX.size() >= 10)
                                listX.remove(0);
                            if(listY.size() >= 10)
                                listY.remove(0);
                            if(listZ.size() >= 10)
                                listZ.remove(0);

                            //Durchschnittswert:
                            double listAvgX = calcListAvg(listX), listAvgY = calcListAvg(listY), listAvgZ = calcListAvg(listZ);
                            System.out.println("listAvgX: " + listAvgX +"\nlistAvgY: " + listAvgY + "\nlistAvgZ: " + listAvgZ);

                            double diffX = (Math.max(listAvgX, x)) - (Math.min(listAvgX, x)); // Immer positiv
                            double diffY = (Math.max(listAvgY, y)) - (Math.min(listAvgY, y));
                            double diffZ = (Math.max(listAvgZ, z)) - (Math.min(listAvgZ, z));

                            LocalDateTime now = LocalDateTime.now();
                            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"); // Geht nur ab API Level 26!
                            String date = now.format(dateTimeFormatter);

                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                            try {
                                Date nowDate = sdf.parse(date);

                                // Wachzeiten:
                                Boolean awake = ( (diffX>peakDiff || diffY>peakDiff || diffZ>peakDiff) || (isAwake && (nowDate.getTime()-tmpDateLast.getTime()>durTimesAwake)) );
                                if(awake) {
                                    if(diffX>peakDiff || diffY>peakDiff || diffZ>peakDiff) {
                                        timesAwakeAll++;
                                        isAwake = true;
                                        tmpDateLast=nowDate;

                                        getActivity().runOnUiThread(new Runnable() {
                                            public void run() {
                                                Toast.makeText(getActivity().getApplicationContext(), "User is Awake!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else if(isAwake && (nowDate.getTime()-tmpDateLast.getTime()>durTimesAwake)){
                                        // Erst wenn man sich x Minuten lang nicht (in dieser Differenz) bewegt hat, wird man als "nicht mehr wach" registriert und die x Minuten Einschlafzeit werden dazuberechnet
                                        timesAwakeAll += awakeDur; // Immer noch x Minuten dazurechnen, bis User "wirklich" eingeschlafen st
                                        isAwake = false;

                                        getActivity().runOnUiThread(new Runnable() {
                                            public void run() {
                                                Toast.makeText(getActivity().getApplicationContext(), "User is Asleep again!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    if(diffX>peakDiff) {
                                        timeOfNumAwakeX.add(date);
                                        timesAwakeX++;
                                    }
                                    if(diffY>peakDiff) {
                                        timeOfNumAwakeY.add(date);
                                        timesAwakeY++;
                                    }
                                    if(diffZ>peakDiff) {
                                        timeOfNumAwakeZ.add(date);
                                        timesAwakeZ++;
                                    }
                                } else { // not awake:
                                    // Leichter Schlaf:
                                    if(diffX>peakDiff2 || diffY>peakDiff2 || diffZ>peakDiff2) {
                                        timesAwakeAll2++;
                                    }

                                    if(diffX>peakDiff2) {
                                        timeOfNumAwakeX2.add(date);
                                        timesAwakeX2++;
                                    }
                                    if(diffY>peakDiff2) {
                                        timeOfNumAwakeY2.add(date);
                                        timesAwakeY2++;
                                    }
                                    if(diffZ>peakDiff2) {
                                        timeOfNumAwakeZ2.add(date);
                                        timesAwakeZ2++;
                                    }
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        System.out.println("Im Mittel alle 3 Sekunden auf der X Achse: " + x);
                        System.out.println("Im Mittel alle 3 Sekunden auf der Y Achse: " + y);
                        System.out.println("Im Mittel alle 3 Sekunden auf der Z Achse: " + z);
                        System.out.println("Abgelaufene Zeit in s: " + delta/1000);

                        movementDataX.clear();
                        movementDataY.clear();
                        movementDataZ.clear();

                        createSensor();
                    }
                }, 0, 3000);

            }
        });

        stopSensorBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if (wakeLock.isHeld())
                    wakeLock.release();
                timer.cancel();
                stopSensorBtn.setEnabled(false);
                startSensorBtn.setEnabled(true);

//                System.out.println("MinX: " + minX + " & MaxX: " + maxX + " & avgX: " + avgX);

                System.out.println("Time of Num Awake X: ");
                for(int i=0; i<timeOfNumAwakeX.size(); i++) {
                    System.out.println(timeOfNumAwakeX);
                }

                addDataToAnalytics();

                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"); // Geht nur ab API Level 26!
                String endString = now.format(dateTimeFormatter);
                addEndTimeDataToFirebase(endString);

                onPause();
            }
        });

        return rootView;
    }

    public void addDataToAnalytics() {
        calcTimeAll(); //timeOfNumAwakeAll für Wachzeiten und leichten Schlaf berechnen

        Analytics analytics = new Analytics(timesAwakeX, timeOfNumAwakeX,
                timesAwakeY, timeOfNumAwakeY,
                timesAwakeZ, timeOfNumAwakeZ,
                timesAwakeAll, timeOfNumAwakeAll);

        Analytics analyticsLight = new Analytics(timesAwakeX2, timeOfNumAwakeX2,
                timesAwakeY2, timeOfNumAwakeY2,
                timesAwakeZ2, timeOfNumAwakeZ2,
                timesAwakeAll2, timeOfNumAwakeAll2);

        // Wachzeiten:
        historyUser.child(textStartingTime).child("Analytics").child("Awake").setValue(analytics);
        // Leichter Schlaf:
        historyUser.child(textStartingTime).child("Analytics").child("LightSleep").setValue(analyticsLight);
    }

    public void calcTimeAll() {
        // Wachzeiten:
        timeOfNumAwakeAll.addAll(timeOfNumAwakeX);
        timeOfNumAwakeAll.addAll(timeOfNumAwakeY);
        timeOfNumAwakeAll.addAll(timeOfNumAwakeZ);

        Set<String> set = new HashSet<>(timeOfNumAwakeAll);
        timeOfNumAwakeAll.clear();
        timeOfNumAwakeAll.addAll(set);

        Collections.sort(timeOfNumAwakeAll);

        // Leichter Schlaf:
        timeOfNumAwakeAll2.addAll(timeOfNumAwakeX2);
        timeOfNumAwakeAll2.addAll(timeOfNumAwakeY2);
        timeOfNumAwakeAll2.addAll(timeOfNumAwakeZ2);

        Set<String> set2 = new HashSet<>(timeOfNumAwakeAll);
        timeOfNumAwakeAll2.clear();
        timeOfNumAwakeAll2.addAll(set2);

        Collections.sort(timeOfNumAwakeAll2);
    }

    public double calcListAvg(ArrayList<Float> list) {
        float sum = 0, avg;
        for(int i=0; i<list.size(); i++) {
            sum += list.get(i);
        }
        avg = sum/list.size();

        return avg;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("startSensorBtnState", startSensorBtn.isEnabled());
    }

    // Folgende Funktion stoppt das Recording bei Fragment Wechsel erfolgreich
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onDestroyView() {

        super.onDestroyView();
        System.out.println("ONDESTROYVIEW");
        if(stopSensorBtn.isEnabled())
        {
            timer.cancel();
            stopSensorBtn.setEnabled(false);
            startSensorBtn.setEnabled(true);

            addDataToAnalytics();

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"); // Geht nur ab API Level 26!
            String endString = now.format(dateTimeFormatter);
            addEndTimeDataToFirebase(endString);

            onPause();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addDataToFirebase(float x, float y, float z/*, long delta*/) {
        MovementInfo movementInfo = new MovementInfo(x, y, z);
//        movementInfo.setX(x);
//        movementInfo.setY(y);
//        movementInfo.setZ(z);
        //movementInfo.setDelta(delta);

        String referenceStr = "MovementInfo/"+(FirebaseAuth.getInstance().getCurrentUser().getUid()+"");
        DatabaseReference databaseReferenceInfo = firebaseDatabase.getReference(referenceStr);

        System.out.println("DATABASEREFERENCEINFO: " + referenceStr);

//        databaseReferenceInfo.addValueEventListener(new ValueEventListener() {
//            @RequiresApi(api = Build.VERSION_CODES.O)
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {

            FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
            assert currentFirebaseUser != null;
            String userChild = currentFirebaseUser.getUid()+"";

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"); // Geht nur ab API Level 26!
            String dateChild = now.format(dateTimeFormatter);

            if(!userChild.isEmpty() && recording)
            {
                // databaseReference = firebaseDatabase.getReference("MovementInfo");

                databaseReference.child(userChild+"").child(dateChild).setValue(movementInfo);
//                    System.out.println("HINZUGEFÜGT!");
//                    return;
                //Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "data added", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "userChild was empty!", Toast.LENGTH_SHORT).show();
            }

//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Fail to add data", Toast.LENGTH_SHORT).show();
//            }
//        });

    }

    public void addStartTimeDataToFirebase(String startingTime) {

        movementTime.setStartingTime(startingTime);

        String referenceStr = "MovementTime/"+(FirebaseAuth.getInstance().getCurrentUser().getUid()+"");
        //DatabaseReference databaseReference2 = firebaseDatabase.getReference(referenceStr);
        System.out.println("databaseReference2: " + referenceStr);

//        databaseReference2.addValueEventListener(new ValueEventListener() {
//            @RequiresApi(api = Build.VERSION_CODES.O)
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        assert currentFirebaseUser != null;
        String userChild = currentFirebaseUser.getUid()+"";

        if(!userChild.isEmpty())
        {
            databaseReferenceTime.child(userChild+"").setValue(movementTime);
            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "startTimeData added", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "userChild was empty!", Toast.LENGTH_SHORT).show();
        }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Fail to add data", Toast.LENGTH_SHORT).show();
//            }
//        });
    }
     public void addEndTimeDataToFirebase(String endingTime) {

            movementTime.setEndingTime(endingTime);

             String referenceStr = "MovementTime/"+(FirebaseAuth.getInstance().getCurrentUser().getUid()+"");
             DatabaseReference databaseReference2 = firebaseDatabase.getReference(referenceStr);

//             databaseReference2.addValueEventListener(new ValueEventListener() {
//                @RequiresApi(api = Build.VERSION_CODES.O)
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {

            FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
            assert currentFirebaseUser != null;
            String userChild = currentFirebaseUser.getUid()+"";

            if(!userChild.isEmpty())
            {
                databaseReferenceTime.child(userChild+"").setValue(movementTime);
                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "endTimeData added", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "userChild was empty!", Toast.LENGTH_SHORT).show();
            }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Fail to add data", Toast.LENGTH_SHORT).show();
//                }
//            });
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

        //System.out.println("ONSENSORCHANGED");
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