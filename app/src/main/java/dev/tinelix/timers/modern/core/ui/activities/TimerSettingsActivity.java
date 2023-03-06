package dev.tinelix.timers.modern.core.ui.activities;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;

import dev.tinelix.timers.modern.R;
import dev.tinelix.timers.modern.core.ui.fragments.TimerSettingsFragment;

public class TimerSettingsActivity extends AppCompatActivity {
    public String old_timer_name;
    public String package_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                old_timer_name = null;
                package_name = null;
            } else {
                old_timer_name = extras.getString("timerName");
                package_name = extras.getString("packageName");
            }
        } else {
            old_timer_name = (String) savedInstanceState.getSerializable("timerName");
            package_name = (String) savedInstanceState.getSerializable("packageName");
        }
        setContentView(R.layout.settings_fragment);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, new TimerSettingsFragment())
                .commit();
        try {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public String getTimerName() {
        return old_timer_name;
    }
}
