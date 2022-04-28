package dev.tinelix.timers.modern.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import dev.tinelix.timers.modern.R;
import dev.tinelix.timers.modern.activities.TimerSettingsActivity;

public class TimerSettingsFragment extends PreferenceFragmentCompat {
    public String old_timer_name;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // FIX padding
        if (getListView() != null) {
            getListView().setPadding(0, 0, 0, 0);
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        final String[] timer_actions = getResources().getStringArray(R.array.timer_actions);
        old_timer_name = ((TimerSettingsActivity) getActivity()).getTimerName();
        addPreferencesFromResource(R.xml.timer_settings);
        if(old_timer_name != null) {
            android.support.v7.preference.Preference timer_name = findPreference("timer_name");
            timer_name.setOnPreferenceClickListener(new android.support.v7.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(android.support.v7.preference.Preference preference) {
                    openEnterDialog("set_timer_name");
                    return false;
                }
            });
            timer_name.setSummary(old_timer_name);
            android.support.v7.preference.Preference timer_action = findPreference("timer_action");
            timer_action.setOnPreferenceClickListener(new android.support.v7.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(android.support.v7.preference.Preference preference) {
                    String current_value = getActivity().getSharedPreferences(old_timer_name, 0).getString("timerAction", "");
                    openChoiceDialog("set_timer_action", timer_actions, current_value);
                    return false;
                }
            });
            final android.support.v7.preference.Preference timerActionDate = findPreference("timerActionDate");
            if(getActivity().getSharedPreferences(old_timer_name, 0).getString("countAction", "").contains("calculateRemainingTime")) {
                timerActionDate.setTitle(getResources().getString(R.string.end_date));
                timer_action.setSummary(timer_actions[0]);
            } else {
                timerActionDate.setTitle(getResources().getString(R.string.start_date));
                timer_action.setSummary(timer_actions[1]);
            }
            timerActionDate.setOnPreferenceClickListener(new android.support.v7.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(android.support.v7.preference.Preference preference) {
                    createDateTimePickerDialog("set_action_date");
                    return false;
                }
            });
            timerActionDate.setSummary(new SimpleDateFormat("d MMMM yyyy HH:mm:ss").format(getActivity().getSharedPreferences(old_timer_name, 0).
                    getLong("timerActionDate", 0)));
        }
    }

    private void createDateTimePickerDialog(String action) {
        if(action.equals("set_action_date")) {
            final android.support.v7.preference.Preference timerActionDate = findPreference("timerActionDate");
            LayoutInflater inflater = getLayoutInflater();
            final boolean[] isInvalidDate = new boolean[1];
            final boolean[] isInvalidTime = new boolean[1];
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            final View view = inflater.inflate(R.layout.datetime_picker_layout, null);
            final EditText date_edit = view.findViewById(R.id.date_edit);
            final EditText time_edit = view.findViewById(R.id.time_edit);
            final TextView error_date_text = view.findViewById(R.id.error_date_text);
            final TextView error_time_text = view.findViewById(R.id.error_time_text);
            final Date[] actionDateEditing = {new Date()};
            final Date[] actionDate = {new Date()};

            builder.setTitle(timerActionDate.getTitle());
            builder.setView(view);
            builder.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        if(time_edit.getText().toString().length() > 0) {
                            if(date_edit.getText().toString().length() > 0) {
                                actionDate[0] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date_edit.getText().toString() + " " + time_edit.getText().toString());
                            } else {
                                actionDate[0] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis())) + " " + time_edit.getText().toString());
                            }
                        } else {
                            actionDate[0] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date_edit.getText().toString() + " 00:00:00");
                        }
                        onChangingTimeValues(old_timer_name, "set_action_time", actionDate[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            builder.setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            final AlertDialog alertDialog = builder.create();
            date_edit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    String[] date_array = date_edit.getText().toString().split("-");
                    if(date_edit.getText().toString().length() == 10) {
                        try {
                            if(date_array.length >= 3) {
                                if (Integer.parseInt(date_array[1]) > 0 && Integer.parseInt(date_array[2]) > 0 && Integer.parseInt(date_array[1]) <= 12 &&
                                        Integer.parseInt(date_array[2]) <= 31) {
                                    actionDateEditing[0] = new SimpleDateFormat("yyyy-MM-dd").parse(date_edit.getText().toString());
                                    long diff = (new Date().getTime() - actionDateEditing[0].getTime());
                                    long elapsed_days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                                    long remaining_days = TimeUnit.DAYS.convert(-diff, TimeUnit.MILLISECONDS);
                                    if(getActivity().getSharedPreferences(old_timer_name, 0).getString("timerAction", "").equals("calculateElapsedTime")) {
                                        if (elapsed_days > 0) {
                                            isInvalidDate[0] = false;
                                        } else {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                                                date_edit.setError(getResources().getString(R.string.invalid_elapsed_date));
                                            else {
                                                error_date_text.setVisibility(View.VISIBLE);
                                                error_date_text.setText(getResources().getString(R.string.invalid_elapsed_date));
                                            }
                                            isInvalidDate[0] = true;
                                        }
                                    } else {
                                        if (remaining_days > 0) {
                                            error_date_text.setVisibility(View.GONE);
                                            isInvalidDate[0] = false;
                                        } else {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                                                date_edit.setError(getResources().getString(R.string.invalid_upcoming_date));
                                            else {
                                                error_date_text.setVisibility(View.VISIBLE);
                                                error_date_text.setText(getResources().getString(R.string.invalid_upcoming_date));
                                            }
                                            isInvalidDate[0] = true;
                                        }
                                    }
                                } else {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                                        date_edit.setError(getResources().getString(R.string.invalid_format));
                                    else {
                                        error_date_text.setVisibility(View.VISIBLE);
                                        error_date_text.setText(getResources().getString(R.string.invalid_format));
                                    }
                                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                                    isInvalidDate[0] = true;
                                }
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                                    date_edit.setError(getResources().getString(R.string.invalid_format));
                                else {
                                    error_date_text.setVisibility(View.VISIBLE);
                                    error_date_text.setText(getResources().getString(R.string.invalid_format));
                                }
                                isInvalidDate[0] = true;
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                                date_edit.setError(getResources().getString(R.string.invalid_format));
                            else {
                                error_date_text.setVisibility(View.VISIBLE);
                                error_date_text.setText(getResources().getString(R.string.invalid_format));
                            }
                            isInvalidDate[0] = true;
                        }
                    } else {
                        isInvalidDate[0] = true;
                    }
                    
                    if(isInvalidDate[0] || isInvalidTime[0]) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    } else {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        error_date_text.setVisibility(View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            time_edit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    String[] time_array = time_edit.getText().toString().split(":");
                    if(time_edit.getText().toString().length() == 8) {
                        try {
                            if(time_array.length >= 3) {
                                if (Integer.parseInt(time_array[0]) <= 23 && Integer.parseInt(time_array[1]) <= 59 && Integer.parseInt(time_array[2]) <= 59) {
                                    actionDateEditing[0] = new SimpleDateFormat("HH:mm:ss").parse(time_edit.getText().toString());
                                    isInvalidTime[0] = false;
                                } else {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                                        time_edit.setError(getResources().getString(R.string.invalid_format));
                                    } else {
                                        error_time_text.setVisibility(View.VISIBLE);
                                        error_time_text.setText(getResources().getString(R.string.invalid_format));
                                    }
                                    isInvalidTime[0] = true;
                                }
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                                    time_edit.setError(getResources().getString(R.string.invalid_format));
                                else {
                                    error_time_text.setVisibility(View.VISIBLE);
                                    error_time_text.setText(getResources().getString(R.string.invalid_format));
                                }
                                isInvalidTime[0] = true;
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                                time_edit.setError(getResources().getString(R.string.invalid_format));
                            else {
                                error_time_text.setVisibility(View.VISIBLE);
                                error_time_text.setText(getResources().getString(R.string.invalid_format));
                            }
                            isInvalidTime[0] = true;
                        }
                    } else {
                        isInvalidTime[0] = true;
                    }

                    if(isInvalidDate[0] || isInvalidTime[0]) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    } else {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        error_time_text.setVisibility(View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            alertDialog.show();
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }

    private void onChangingTimeValues(String timer_name, String action, Date date) {
        if(action.equals("set_action_time")) {
            long date_sec = date.getTime();
            SharedPreferences.Editor editor = getActivity().getSharedPreferences(timer_name, 0).edit();
            editor.putLong("timerActionDate", date_sec);
            editor.commit();
            final android.support.v7.preference.Preference timerActionDate = findPreference("timerActionDate");
            timerActionDate.setSummary(new SimpleDateFormat("d MMMM yyyy HH:mm:ss").format(date));
        }
    }

    private void openChoiceDialog(String action, String[] array, String value) {
        if(action.equals("set_timer_action")) {
            LayoutInflater inflater = getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(getResources().getString(R.string.timer_action_pref));
            if(value.equals("calculateRemainingTime")) {
                builder.setSingleChoiceItems(array, 0,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                String value;
                                if(item == 0) {
                                    value = "calculateRemainingTime";
                                } else {
                                    value = "calculateElapsedTime";
                                }
                                onChangingValues(old_timer_name, "timerAction", value);
                                android.support.v7.preference.Preference timerActionDate = findPreference("timerActionDate");
                                timerActionDate.setTitle(getResources().getString(R.string.end_date));
                            }
                        });
            } else {
                builder.setSingleChoiceItems(array, 1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                String value;
                                if(item == 0) {
                                    value = "calculateRemainingTime";
                                } else {
                                    value = "calculateElapsedTime";
                                }
                                onChangingValues(old_timer_name, "timerAction", value);
                                android.support.v7.preference.Preference timerActionDate = findPreference("timerActionDate");
                                timerActionDate.setTitle(getResources().getString(R.string.start_date));
                            }
                        });
            }
            builder.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            final AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    public void onChangingValues(String timer_name, String param, String value) {
        try {
            SharedPreferences prefs = getActivity().getSharedPreferences(timer_name, 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(param, value);
            editor.commit();
            if(param.equals("timerAction")) {
                if(value.equals("calculateRemainingTime")) {
                    final String[] timer_actions = getResources().getStringArray(R.array.timer_actions);
                    final android.support.v7.preference.Preference timer_action = findPreference("timer_action");
                    timer_action.setSummary(timer_actions[0]);
                } else {
                    final String[] timer_actions = getResources().getStringArray(R.array.timer_actions);
                    final android.support.v7.preference.Preference timer_action = findPreference("timer_action");
                    timer_action.setSummary(timer_actions[1]);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openEnterDialog(String action) {
        if (action.equals("set_timer_name")) {
            LayoutInflater inflater = getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            final View view = inflater.inflate(R.layout.enter_text_layout, null);
            builder.setView(view);
            final EditText value_edit = view.findViewById(R.id.enter_value);
            final TextView error_text = view.findViewById(R.id.error_text);
            builder.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String profile_path = "/data/data/" + getContext().getPackageName() + "/shared_prefs/" + old_timer_name + ".xml";
                    File file = new File(profile_path);
                    file.delete();
                    old_timer_name = value_edit.getText().toString();
                    android.support.v7.preference.Preference timer_name = findPreference("timer_name");
                    timer_name.setSummary(value_edit.getText().toString());
                    SharedPreferences prefs = getActivity().getSharedPreferences(value_edit.getText().toString(), 0);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong("createdDate", System.currentTimeMillis());
                    editor.putString("timerAction", "calculateRemainingTime");
                    editor.putLong("timerActionDate", System.currentTimeMillis());
                    editor.commit();
                }
            });
            builder.setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            final AlertDialog alertDialog = builder.create();
            value_edit.setHint(getResources().getString(R.string.name));
            value_edit.setText(old_timer_name);
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
}
