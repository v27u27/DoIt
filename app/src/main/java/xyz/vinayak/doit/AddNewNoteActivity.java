package xyz.vinayak.doit;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddNewNoteActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    TextView tvSetReminderDateTime;
    EditText etNoteTitle, etNoteDescription;
    ImageView ivReminder;
    Spinner spinner;
    FloatingActionButton fab;
    Calendar reminderDateTimePicked;

    String reminderDate, reminderTime;
    private boolean isReached = false;
    String noteTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_note);

        etNoteTitle = findViewById(R.id.etNewNoteTitle);
        etNoteDescription = findViewById(R.id.etNewNoteDescription);
        spinner = findViewById(R.id.spinnerNoteCategory);
        fab = findViewById(R.id.fabDone);
        ivReminder = findViewById(R.id.ivSetReminderIcon);
        tvSetReminderDateTime = findViewById(R.id.tvSetReminderDateTime);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        final List<String> categories = new ArrayList<String>();
        categories.add("Urgent");
        categories.add("Work");
        categories.add("Personal");
        categories.add("Others");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setPrompt("Category");
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);


        ivReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(AddNewNoteActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.setMinDate(now);
                dpd.setVersion(DatePickerDialog.Version.VERSION_2);
                dpd.show(getFragmentManager(), "DatePickerDialog");

            }
        });

        ivReminder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                reminderDateTimePicked = null;
                ivReminder.setImageResource(R.drawable.ic_reminder_24dp);
                tvSetReminderDateTime.setVisibility(View.GONE);
                Toast.makeText(AddNewNoteActivity.this, "Reminder Deleted", Toast.LENGTH_SHORT).show();
                return true;
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteTitle = etNoteTitle.getText().toString();
                String noteDescription = etNoteDescription.getText().toString();

                if (noteTitle.trim().isEmpty()) {
                    noteTitle = "Untitled Task";
                } else {
                    noteTitle = getModifiedNoteTile(noteTitle);
                }
                if (noteDescription.trim().isEmpty()) {
                    noteDescription = " ";
                }

                int category = spinner.getSelectedItemPosition();
                // category == 0 for Urgent
                // category == 1 for Work
                // category == 2 for Personal
                // category == 3 for Others


                Todo newTodo = new Todo(System.currentTimeMillis(), noteTitle, getDateTime(), noteDescription, category);
                if (reminderDateTimePicked != null) {
                    newTodo.setReminderDateTime(reminderDateTimePicked.getTimeInMillis());
                    setReminderAlarm(reminderDateTimePicked);
                }

                Intent output = new Intent();
                output.putExtra("newtodo", newTodo);
                setResult(RESULT_OK, output);
                finish();
            }
        });
    }

    @SuppressLint("NewApi")
    private void setReminderAlarm(Calendar reminderDateTimePicked) {
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("noteTitle", noteTitle);
        alarmIntent.putExtra("reminderTime", reminderDateTimePicked.getTimeInMillis());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderDateTimePicked.getTimeInMillis(), pendingIntent);
    }

    private String getModifiedNoteTile(String s) {
        int length = s.length();
        int upperLimit = 30;
        int lowerLimit = 0;
        int limit = upperLimit;
        while (upperLimit < length && (s.charAt(limit)) != ' ') {
            if (s.charAt(limit) == ' ') {
                s = s.substring(lowerLimit, limit) + '\n' + s.substring(limit + 1);
                lowerLimit = limit + 1;
                limit = upperLimit + 1;
                upperLimit += 30;
            } else
                limit--;
        }
        return s;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private String getDateTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy hh:mm aaa");
        return df.format(c.getTime());
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        reminderDate = "" + dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
        Log.e("TAG", "onDateSet: " + reminderDate);
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                AddNewNoteActivity.this,
                now.get(Calendar.HOUR),
                now.get(Calendar.MINUTE),
                false
        );
        tpd.setVersion(TimePickerDialog.Version.VERSION_2);
        tpd.show(getFragmentManager(), "TimePickerDialog");
//        dateTextView.setText(date);
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        reminderTime = "" + hourOfDay + ":" + minute;
        Log.e("TAG", "onTimeSet: " + reminderTime);

        reminderDateTimePicked = Calendar.getInstance();

        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);

        try {
            reminderDateTimePicked.setTime(format.parse(reminderDate + " " + reminderTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ivReminder.setImageResource(R.drawable.ic_alarm_blue_24dp);
        tvSetReminderDateTime.setText("You have set Reminder for " + getReminderDateTime(reminderDateTimePicked));
        tvSetReminderDateTime.setVisibility(View.VISIBLE);
    }

    private String getReminderDateTime(Calendar c) {
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy hh:mm aaa");
        return df.format(c.getTime());
    }
}


