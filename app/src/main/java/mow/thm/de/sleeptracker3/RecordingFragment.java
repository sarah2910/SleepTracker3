package mow.thm.de.sleeptracker3;

import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecordingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button startbtn, stopbtn;                           //Die Start und Stop Buttons
    private MediaRecorder recorder;                             //Der Recorder
    private static final String LOG_TAG = "AudioRecording";     //...
    private static String mFileName = null;                     //Der noch leere Dateiname
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;  //...

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

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile("PATH_NAME");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_recording, container, false);

        startbtn = (Button)rootView.findViewById(R.id.btnRecord);
        stopbtn = (Button)rootView.findViewById(R.id.btnStop);
        stopbtn.setEnabled(false);

        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += " /AudioRecording.3gp";

        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CheckPermissions()) {

                }

            }
        });


        return rootView;
    }

    public boolean CheckPermissions() {

        return true;
    }
}