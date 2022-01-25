package mow.thm.de.sleeptracker3;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // button for logout and initialing our button.
        //Button logoutBtn = findViewById(R.id.idBtnLogout);
        Button logoutBtn = view.findViewById(R.id.idBtnLogout);
        Button deleteBtn = view.findViewById(R.id.idBtnDelete);
        Button deleteAllBtn = view.findViewById(R.id.idBtnDeleteAll);

        // adding onclick listener for our logout button.
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // below line is for getting instance
                // for AuthUi and after that calling a
                // sign out method from FIrebase.
                AuthUI.getInstance()
                        //.signOut(HomeActivity.this)
                        .signOut(Objects.requireNonNull(getActivity()))

                        // after sign out is executed we are redirecting
                        // our user to MainActivity where our login flow is being displayed.
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {

                                // below method is used after logout from device.
                                //Toast.makeText(HomeActivity.this, "User Signed Out", Toast.LENGTH_SHORT).show();
                                Toast.makeText(Objects.requireNonNull(getActivity()), "User Signed Out", Toast.LENGTH_SHORT).show();

                                // below line is to go to MainActivity via an intent.
                                //Intent i = new Intent(HomeActivity.this, MainActivity.class);
                                Intent i = new Intent(Objects.requireNonNull(getActivity()), MainActivity.class);
                                startActivity(i);
                            }
                        });
            }

        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
                assert currentFirebaseUser != null;
                String userChild = currentFirebaseUser.getUid()+"";

                DatabaseReference databaseReferenceUser = firebaseDatabase.getReference("MovementInfo").child(userChild+"");

                databaseReferenceUser.removeValue();

            }

        });

        deleteAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;

                if(currentFirebaseUser != null) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Are you sure you want to delete ALL User Data?");

                    builder.setPositiveButton(Html.fromHtml("<font color='#FFFFFF'>YES</font>"), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String userChild = currentFirebaseUser.getUid() + "";
                            DatabaseReference databaseReferenceMovementInfo = firebaseDatabase.getReference("MovementInfo").child(userChild + "");
                            DatabaseReference databaseReferenceMovementTime = firebaseDatabase.getReference("MovementTime").child(userChild + "");
                            DatabaseReference databaseReferenceHistory = firebaseDatabase.getReference("History").child(userChild + "");

                            databaseReferenceMovementInfo.removeValue();
                            databaseReferenceMovementTime.removeValue();
                            databaseReferenceHistory.removeValue();

                            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "All User Data deleted!", Toast.LENGTH_SHORT).show();

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

            }

        });

        return view;
    }
}