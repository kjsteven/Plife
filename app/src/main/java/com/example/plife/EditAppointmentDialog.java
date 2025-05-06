package com.example.plife;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditAppointmentDialog extends Dialog {

    private EditText editTextDate;
    private EditText editTextTime;
    private Button buttonSave;
    private Button buttonCancel;

    private Appointment currentAppointment;

    private DatabaseReference databaseReference;

    public EditAppointmentDialog(@NonNull Context context, Appointment appointment) {
        super(context);
        currentAppointment = appointment;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Appointment").child("users").child(userId);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_appointment);

        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);

        // Set the current date and time in the dialog
        editTextDate.setText(currentAppointment.getDate());
        editTextTime.setText(currentAppointment.getTime());

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        editTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChanges();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelChanges();
            }
        });

    }

    private void cancelChanges() {
        dismiss(); // Dismiss the dialog
    }

    public void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Format the date as dd/MM/yyyy
                        String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year);
                        editTextDate.setText(selectedDate);
                    }
                }, year, month, day);

        datePickerDialog.show();
    }


    public void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Use SimpleDateFormat to format time with AM/PM
                        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                        Calendar selectedTime = Calendar.getInstance();
                        selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedTime.set(Calendar.MINUTE, minute);
                        String formattedTime = timeFormat.format(selectedTime.getTime());

                        editTextTime.setText(formattedTime);
                    }
                }, hour, minute, false); // Use 24-hour format

        timePickerDialog.show();
    }

    public void show() {
        // Set the content view
        setContentView(R.layout.dialog_edit_appointment);

        // Find your views and set their values

        // Set the width and height of the dialog window
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT; // Set the width you desire
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT; // Set the height you desire

        // Show the dialog
        super.show();

        // Set the attributes after showing the dialog to take effect
        getWindow().setAttributes(layoutParams);
    }

    private void saveChanges() {
        String newDate = editTextDate.getText().toString().trim();
        String newTime = editTextTime.getText().toString().trim();

        if (TextUtils.isEmpty(newDate) || TextUtils.isEmpty(newTime)) {
            Toast.makeText(getContext(), "Date and Time cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the new date is after the current date of the appointment
        if (isDateAfterCurrent(newDate, currentAppointment.getDate())) {
            // Check if the selected date is Monday to Friday and the time is between 8 am and 5 pm
            boolean isWeekday = isWeekday(newDate);
            boolean isTimeValid = isTimeInRange(newTime);

            if (isWeekday && isTimeValid) {
                // Update the appointment details in the Firebase Realtime Database
                String appointmentKey = currentAppointment.getKey();
                DatabaseReference appointmentRef = databaseReference.child(appointmentKey);

                appointmentRef.child("date").setValue(newDate);
                appointmentRef.child("time").setValue(newTime);

                // Dismiss the dialog
                dismiss();
            } else {
                // Display separate messages for date and time restrictions
                if (!isWeekday) {
                    Toast.makeText(getContext(), "Please choose a date between Monday and Friday", Toast.LENGTH_SHORT).show();
                }

                if (!isTimeValid) {
                    Toast.makeText(getContext(), "Please choose a time between 8 am and 5 pm", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(getContext(), "Please choose a date that comes after the current appointment date", Toast.LENGTH_SHORT).show();
        }
    }


    private boolean isWeekday(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date dateObj = sdf.parse(date);

            // Check if the selected date is Monday to Friday (1 to 5)
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateObj);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            return dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY;
        } catch (ParseException e) {
            e.printStackTrace();
            return false; // Return false in case of an error or invalid date format
        }
    }

    private boolean isTimeInRange(String time) {
        try {
            // Use SimpleDateFormat to parse time with AM/PM
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date timeObj = sdf.parse(time);

            // Check if the selected time is between 8 am and 5 pm
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(timeObj);
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            return hourOfDay >= 8 && hourOfDay <= 17; // 8 am to 5 pm
        } catch (ParseException e) {
            e.printStackTrace();
            return false; // Return false in case of an error or invalid time format
        }
    }

    private boolean isDateAfterCurrent(String newDate, String currentDate) {
        try {
            // Parse the new date and current date strings into Date objects
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date newDateObj = sdf.parse(newDate);
            Date currentDateObj = sdf.parse(currentDate);



            // Check if the new date is after the current date
            return newDateObj != null && currentDateObj != null && newDateObj.after(currentDateObj);
        } catch (ParseException e) {
            e.printStackTrace();
            return false; // Return false in case of an error or invalid date format
        }
    }


}
