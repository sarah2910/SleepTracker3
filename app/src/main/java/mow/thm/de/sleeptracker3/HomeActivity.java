package mow.thm.de.sleeptracker3;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNavigationView;

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_container , new RecordingFragment()).commit();

        bottomNavigationView.setSelectedItemId(R.id.nav_recording);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                switch(item.getItemId()) {
                    case R.id.nav_recording:
                        fragment = new RecordingFragment();
                        break;
                    case R.id.nav_hypnogram:
                        ArrayList<String> timeOfNumAwakeX = new ArrayList<>();
                        timeOfNumAwakeX = RecordingFragment.timeOfNumAwakeX;
                        fragment = new HypnogramFragment();
                        Bundle args = new Bundle();
                        args.putStringArrayList("TimeOfNumAwakeX", timeOfNumAwakeX);
                        fragment.setArguments(args);
                        break;
                    case R.id.nav_evaluation:
                        fragment = new EvaluationFragment();
                        break;
                    case R.id.nav_settings:
                        fragment = new SettingsFragment();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.main_container , fragment).commit();

                return true;
            }

        });
    }
}


