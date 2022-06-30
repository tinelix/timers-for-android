package dev.tinelix.timers.modern.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;

import dev.tinelix.timers.modern.R;
import dev.tinelix.timers.modern.list_adapters.TemplateListAdapter;
import dev.tinelix.timers.modern.list_adapters.TimersListAdapter;
import dev.tinelix.timers.modern.list_items.TemplateItem;
import dev.tinelix.timers.modern.list_items.TimerItem;

public class MainActivity extends AppCompatActivity {

    public List<String> timersArray = new ArrayList<String>();
    public ArrayList<TimerItem> timersList = new ArrayList<TimerItem>();
    public Timer timer;
    public Handler handler;
    public Runnable updateTimerUI;
    public AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addTimerButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEnterDialog("create_timer");
            }
        });

        handler = new Handler();
        final RecyclerView timersRecyclerView = (RecyclerView) findViewById(R.id.timer_list);
        updateTimerUI = new Runnable() {
            @Override
            public void run() {
                updateTimersView(timersRecyclerView);
                handler.postDelayed(this, 1000);
            }
        };

        appendTimerItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        appendTimerItems();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.about_app_item) {
            Intent intent = new Intent(MainActivity.this, AboutApplicationActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void appendTimerItems() {
        timersArray.clear();
        timersList.clear();
        String package_name = getApplicationContext().getPackageName();
        String profile_path = "/data/data/" + package_name + "/shared_prefs";
        File prefs_directory = new File(profile_path);
        File[] prefs_files = prefs_directory.listFiles();
        RecyclerView timersRecyclerView = (RecyclerView) findViewById(R.id.timer_list);
        LinearLayout timersLinearLayout = (LinearLayout) findViewById(R.id.empty_list_ll);
        timersArray = new LinkedList<String>();
        String file_extension;
        try {
            for (int i = 0; i < prefs_files.length; i++) {
                if(prefs_files[i].getName().substring(0, (int) (prefs_files[i].getName().length() - 4)).startsWith(getApplicationInfo().packageName + "_preferences"))
                {

                } else {
                    SharedPreferences prefs = MainActivity.this.getSharedPreferences(prefs_files[i].getName().substring(0, (int) (prefs_files[i].getName().length() - 4)), 0);
                    file_extension = prefs_files[i].getName().substring((int) (prefs_files[i].getName().length() - 4));
                    if (file_extension.contains(".xml") && file_extension.length() == 4) {
                        timersList.add(new TimerItem(prefs_files[i].getName().substring(0, (int) (prefs_files[i].getName().length() - 4)),
                                prefs_files[i].getName().substring(0, (int) (prefs_files[i].getName().length() - 4)), prefs.getString("timerAction", ""), prefs.getLong("timerActionDate", 0), "timer"));
                    }
                }
            }
            if(prefs_files == null || timersList.size() == 0) {
                timersRecyclerView.setVisibility(View.GONE);
                timersLinearLayout.setVisibility(View.VISIBLE);
            } else {
                timersRecyclerView.setVisibility(View.VISIBLE);
                timersLinearLayout.setVisibility(View.GONE);
            }
            TimersListAdapter timersListAdapter = new TimersListAdapter(MainActivity.this, timersList);
            timersRecyclerView.setLayoutManager(new GridLayoutManager(this,1));
            if(timersRecyclerView.getAdapter() == null) {
                timersRecyclerView.setAdapter(timersListAdapter);
            } else {
                timersRecyclerView.getAdapter().notifyDataSetChanged();
            }
            handler.removeCallbacks(updateTimerUI);
            handler.post(updateTimerUI);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(updateTimerUI);
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        handler.post(updateTimerUI);
        super.onPostResume();
    }

    private void updateTimersView(RecyclerView recyclerView) {
        if(recyclerView.getAdapter() == null) {
            TimersListAdapter timersListAdapter = new TimersListAdapter(MainActivity.this, timersList);
            recyclerView.setAdapter(timersListAdapter);
        } else {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private void openEnterDialog(String action) {
        if (action.equals("create_timer")) {
            LayoutInflater inflater = getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            final View view = inflater.inflate(R.layout.enter_timer_time_layout, null);
            builder.setView(view);
            final EditText value_edit = view.findViewById(R.id.enter_value);
            final TextView error_text = view.findViewById(R.id.error_text);
            ArrayList<TemplateItem> templateItems = new ArrayList<>();
            templateItems.add(new TemplateItem("newYear", getResources().getString(R.string.new_year)));
            templateItems.add(new TemplateItem("BoW", getResources().getString(R.string.beginning_of_winter)));
            templateItems.add(new TemplateItem("BoSp", getResources().getString(R.string.beginning_of_spring)));
            templateItems.add(new TemplateItem("BoSu", getResources().getString(R.string.beginning_of_summer)));
            templateItems.add(new TemplateItem("BoA", getResources().getString(R.string.beginning_of_autumn)));
            templateItems.add(new TemplateItem("BoY", getResources().getString(R.string.beginning_of_year)));
            TemplateListAdapter templateListAdapter = new TemplateListAdapter(MainActivity.this, templateItems);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            RecyclerView templates = view.findViewById(R.id.templates_rv);
            templates.setLayoutManager(layoutManager);
            templates.setAdapter(templateListAdapter);
            builder.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SharedPreferences prefs = getSharedPreferences(value_edit.getText().toString(), 0);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong("createdDate", System.currentTimeMillis());
                    editor.putString("timerAction", "calculateRemainingTime");
                    editor.putLong("timerActionDate", System.currentTimeMillis());
                    editor.commit();
                    Intent intent = new Intent(MainActivity.this, TimerSettingsActivity.class);
                    intent.putExtra("timerName", value_edit.getText().toString());
                    intent.putExtra("packageName", getApplicationContext().getPackageName());
                    startActivity(intent);

                }
            });
            builder.setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            alertDialog = builder.create();
            value_edit.setHint(getResources().getString(R.string.name));
            value_edit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if(value_edit.getText().toString().contains("/")) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            value_edit.setError(getResources().getString(R.string.text_field_wrong_characters));
                        } else {
                            error_text.setText(getResources().getString(R.string.text_field_wrong_characters));
                            error_text.setVisibility(View.VISIBLE);
                        }
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    } else {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        error_text.setVisibility(View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            alertDialog.show();
        }
    }

    public void createTimerFromTemplate(String name) {
        try {
            if(name.equals("newYear")) {
                SharedPreferences prefs = getSharedPreferences(getResources().getString(R.string.new_year), 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("createdDate", System.currentTimeMillis());
                editor.putString("timerAction", "calculateRemainingTime");
                Date currentDate = new Date();
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(currentDate);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date newYearDate = simpleDateFormat.parse(String.format("%d-01-01 00:00:00", calendar.get(Calendar.YEAR) + 1));
                editor.putLong("timerActionDate", newYearDate.getTime());
                editor.commit();
                onResume();
                if(alertDialog != null) {
                    alertDialog.cancel();
                }
            } else if(name.equals("BoW")) {
                SharedPreferences prefs = getSharedPreferences(getResources().getString(R.string.beginning_of_winter), 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("createdDate", System.currentTimeMillis());
                Date currentDate = new Date();
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(currentDate);
                Date BoWDate = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                BoWDate = simpleDateFormat.parse(String.format("%d-12-01 00:00:00", calendar.get(Calendar.YEAR)));
                if((new Date().getTime() - BoWDate.getTime()) <= 7776000 && (new Date().getTime() - BoWDate.getTime()) >= 0) {
                    editor.putString("timerAction", "calculateElapsedTime");
                    BoWDate = simpleDateFormat.parse(String.format("%d-12-01 00:00:00", calendar.get(Calendar.YEAR)));
                    editor.putLong("timerActionDate", BoWDate.getTime());
                } else {
                    editor.putString("timerAction", "calculateRemainingTime");
                    BoWDate = simpleDateFormat.parse(String.format("%d-12-01 00:00:00", calendar.get(Calendar.YEAR)));
                    editor.putLong("timerActionDate", BoWDate.getTime());
                }
                editor.commit();
                onResume();
                if(alertDialog != null) {
                    alertDialog.cancel();
                }
            } else if(name.equals("BoSp")) {
                SharedPreferences prefs = getSharedPreferences(getResources().getString(R.string.beginning_of_spring), 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("createdDate", System.currentTimeMillis());
                Date currentDate = new Date();
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(currentDate);
                Date BoSpDate = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                BoSpDate = simpleDateFormat.parse(String.format("%d-03-01 00:00:00", calendar.get(Calendar.YEAR)));
                if((new Date().getTime() - BoSpDate.getTime()) >= 0) {
                    editor.putString("timerAction", "calculateElapsedTime");
                    BoSpDate = simpleDateFormat.parse(String.format("%d-03-01 00:00:00", calendar.get(Calendar.YEAR)));
                    editor.putLong("timerActionDate", BoSpDate.getTime());
                } else {
                    editor.putString("timerAction", "calculateRemainingTime");
                    BoSpDate = simpleDateFormat.parse(String.format("%d-03-01 00:00:00", calendar.get(Calendar.YEAR)));
                    editor.putLong("timerActionDate", BoSpDate.getTime());
                }
                editor.commit();
                onResume();
                if(alertDialog != null) {
                    alertDialog.cancel();
                }
            } else if(name.equals("BoSu")) {
                SharedPreferences prefs = getSharedPreferences(getResources().getString(R.string.beginning_of_summer), 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("createdDate", System.currentTimeMillis());
                Date currentDate = new Date();
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(currentDate);
                Date BoSuDate = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                BoSuDate = simpleDateFormat.parse(String.format("%d-03-01 00:00:00", calendar.get(Calendar.YEAR)));
                if((new Date().getTime() - BoSuDate.getTime()) >= 0) {
                    editor.putString("timerAction", "calculateElapsedTime");
                    BoSuDate = simpleDateFormat.parse(String.format("%d-06-01 00:00:00", calendar.get(Calendar.YEAR)));
                    editor.putLong("timerActionDate", BoSuDate.getTime());
                } else {
                    editor.putString("timerAction", "calculateRemainingTime");
                    BoSuDate = simpleDateFormat.parse(String.format("%d-06-01 00:00:00", calendar.get(Calendar.YEAR)));
                    editor.putLong("timerActionDate", BoSuDate.getTime());
                }
                editor.commit();
                onResume();
                if(alertDialog != null) {
                    alertDialog.cancel();
                }
            } else if(name.equals("BoA")) {
                SharedPreferences prefs = getSharedPreferences(getResources().getString(R.string.beginning_of_autumn), 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("createdDate", System.currentTimeMillis());
                Date currentDate = new Date();
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(currentDate);
                Date BoADate = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                BoADate = simpleDateFormat.parse(String.format("%d-09-01 00:00:00", calendar.get(Calendar.YEAR)));
                if((new Date().getTime() - BoADate.getTime()) >= 0) {
                    editor.putString("timerAction", "calculateElapsedTime");
                    BoADate = simpleDateFormat.parse(String.format("%d-09-01 00:00:00", calendar.get(Calendar.YEAR)));
                    editor.putLong("timerActionDate", BoADate.getTime());
                } else {
                    editor.putString("timerAction", "calculateRemainingTime");
                    BoADate = simpleDateFormat.parse(String.format("%d-09-01 00:00:00", calendar.get(Calendar.YEAR)));
                    editor.putLong("timerActionDate", BoADate.getTime());
                }
                editor.commit();
                onResume();
                if(alertDialog != null) {
                    alertDialog.cancel();
                }
            } if(name.equals("BoY")) {
                SharedPreferences prefs = getSharedPreferences(getResources().getString(R.string.beginning_of_year), 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("createdDate", System.currentTimeMillis());
                editor.putString("timerAction", "calculateElapsedTime");
                Date currentDate = new Date();
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(currentDate);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date BoYDate = simpleDateFormat.parse(String.format("%d-01-01 00:00:00", calendar.get(Calendar.YEAR)));
                editor.putLong("timerActionDate", BoYDate.getTime());
                editor.commit();
                onResume();
                if(alertDialog != null) {
                    alertDialog.cancel();
                }
            }
        } catch (Exception ex) {

        }
    }

    public void showTimerEditorDialog(int position) {
        TimerItem timerItem = timersList.get(position);
        Intent intent = new Intent(MainActivity.this, TimerSettingsActivity.class);
        intent.putExtra("timerName", timerItem.name);
        intent.putExtra("packageName", getApplicationContext().getPackageName());
        startActivity(intent);
    }

    public void deleteTimer(int position) {
        final TimerItem timerItem = timersList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(R.string.delete_timer_question);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    String profile_path = "/data/data/" + getPackageName() + "/shared_prefs/" + timerItem.name + ".xml";
                    File file = new File(profile_path);
                    file.delete();
                    appendTimerItems();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
