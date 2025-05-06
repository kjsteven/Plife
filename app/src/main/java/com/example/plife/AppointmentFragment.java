package com.example.plife;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AppointmentFragment extends Fragment {

    private EditText dateEditText;
    private EditText timeEditText;
    private Spinner servicesSpinner;

    private AutoCompleteTextView descriptionInput;
    private DatabaseReference databaseReference;
    private Calendar selectedDateTime;
    private boolean appointmentExistsOnSelectedDay = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment_, container, false);

        // Initialize the database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Appointment");

        dateEditText = view.findViewById(R.id.dateEditText);
        timeEditText = view.findViewById(R.id.timeEditText);
        servicesSpinner = view.findViewById(R.id.servicesSpinner);
        descriptionInput = view.findViewById(R.id.descriptionInput);


        // Services data
        String[] servicesArray = {"Select Service", "Vaccination", "Maternal and Child Health Care", "Nutrition Counseling", "Medical Consultations", "Dental Services", "Lab Request"
        , "TB Dots", "PWD Signing", "Medical Clearance", "Wound Cleaning / Burns"};
        // Services Spinner setup
        ArrayAdapter<String> servicesAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, servicesArray);
        servicesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        servicesSpinner.setAdapter(servicesAdapter);

        // Service Descriptions mapping
        Map<String, String[]> descriptionsMap = new HashMap<>();
        descriptionsMap.put("Vaccination", new String[]{
                "For well babies' vaccination program.",
                "Tetanus toxoid vaccination, etc.",
                "Other supplemental vaccination programs are provided by the Department of Health (DOH)."
        });
        descriptionsMap.put("Maternal and Child Health Care", new String[]{
                "Offers prenatal care, including regular check-ups.",
                "Provides postnatal care to ensure the well-being.",
                "Conducts well-baby clinics for growth monitoring.",
                "Offers family planning services to empower couples."
        });
        descriptionsMap.put("Nutrition Counseling", new String[]{
                "Delivers personalized nutrition counseling for individuals with specific dietary needs.",
                "Conducts weight management programs for those at risk of malnutrition or obesity",
                "Offers nutritional support for pregnant women, lactating mothers, and infants.",
                "Provides education on healthy eating habits and the importance of a balanced diet."
        });
        descriptionsMap.put("Medical Consultations", new String[]{
                "General Medical Consultations.",
                "Provide diagnosis and treatment."


        });
        descriptionsMap.put("Dental Services", new String[]{
                "Tooth Extraction",
                "Dental Checkup",
                "Temporary Filling",
                "Fluoride Application"
        });
        descriptionsMap.put("Lab Request", new String[]{
                "To determine subjective options based on the doctor's assessment or evaluation."
        });
        descriptionsMap.put("TB Dots", new String[]{
                "Tuberculosis treatment program for patients diagnosed with TB."
        });
        descriptionsMap.put("PWD Signing", new String[]{
                "To determine if the person qualifies as a PWD (Person with Disability)."
        });
        descriptionsMap.put("Medical Clearance", new String[]{
                "Signing off on medical clearances such as pre-employment, fit-to-work, fit-to-return-to-school, \nand other medical clearances, etc."
        });
        descriptionsMap.put("Wound Cleaning / Burns", new String[]{
                "Minor burns and wound cleaning."
        });



        // Set up a listener for the Services Spinner
        servicesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Handle the selected item
                String selectedService = servicesArray[position];

                // Clear the AutoCompleteTextView when a service is chosen
                descriptionInput.setText("");

                // Set the AutoCompleteTextView to be clickable to show the dialog again
                descriptionInput.setClickable(true);

                // Set up a listener for the descriptionInput
                descriptionInput.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Show the dialog when the user clicks on the descriptionInput
                        String[] descriptions = descriptionsMap.get(selectedService);
                        if (descriptions != null) {
                            showServiceDescriptionDialog(descriptions);
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });


        // Set up a listener for the descriptionInput
        descriptionInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the dialog when the user clicks on the descriptionInput
                String selectedService = servicesArray[servicesSpinner.getSelectedItemPosition()];
                String[] descriptions = descriptionsMap.get(selectedService);
                if (descriptions != null) {
                    showServiceDescriptionDialog(descriptions);
                }
            }
        });




        Button selectDateButton = view.findViewById(R.id.datePicker);
        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show DatePicker dialog
                showDatePickerDialog();
            }
        });

        Button selectTimeButton = view.findViewById(R.id.timePicker);
        selectTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show TimePicker dialog
                showTimePickerDialog();
            }
        });


        Button createAppointmentButton = view.findViewById(R.id.createAppointmentButton);
        createAppointmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceSelected()) {
                    if (isServiceDescriptionSelected()) {
                        if (isDateAndTimeValid()) {
                            if (isTitleAndDescriptionValid()) {
                                // Show a loading dialog
                                showLoadingDialog();

                                // Create the appointment (in this example, we save it to Firebase)
                                checkAppointmentExistsOnSelectedDay();
                            } else {
                                // Show an error message for title and description
                                Toast.makeText(getActivity(), "Please fill in the title and description.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Show an error message for date and time
                            Toast.makeText(getActivity(), "Please select a future date and time.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Show an error message for not selecting service description
                        Toast.makeText(getActivity(), "Please select a service description.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Show an error message for not selecting service
                    Toast.makeText(getActivity(), "Please select a service.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return view;
    }

    private void checkAppointmentExistsOnSelectedDay() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String selectedDate = dateEditText.getText().toString();

        // Check if there is already an appointment on the selected day
        Query existingAppointmentQuery = databaseReference.child("users")
                .child(userId)
                .orderByChild("date")
                .equalTo(selectedDate);

        existingAppointmentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    progressDialog.dismiss();
                    // There is an existing appointment on the selected day
                    appointmentExistsOnSelectedDay = true;
                    Toast.makeText(getActivity(), "You already have an appointment on this day. Please choose another day.", Toast.LENGTH_SHORT).show();
                } else {
                    // No existing appointment on the selected day
                    appointmentExistsOnSelectedDay = false;

                    // Continue with the appointment creation logic
                    createAppointment();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Log.e("CheckAppointment", "Error checking existing appointment", error.toException());
                Toast.makeText(getActivity(), "Error checking existing appointment. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showServiceDescriptionDialog(String[] descriptions) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Service Description");

        builder.setItems(descriptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Set the selected description to the descriptionInput
                descriptionInput.setText(descriptions[which]);
            }
        });

        builder.show();
    }

    private boolean isServiceSelected() {
        return servicesSpinner.getSelectedItemPosition() != 0;
    }

    private boolean isServiceDescriptionSelected() {
        return !TextUtils.isEmpty(descriptionInput.getText().toString().trim());
    }



    private boolean isTitleAndDescriptionValid() {
        String title = servicesSpinner.getSelectedItem().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        return !title.isEmpty() && !description.isEmpty();
    }

    private boolean isDateAndTimeValid() {
        // Check if a valid date and time are selected
        if (selectedDateTime == null || selectedDateTime.before(Calendar.getInstance())) {
            showToast("Please select a future date and time.");
            return false;
        }

        // Check if the selected day is Monday to Friday
        int dayOfWeek = selectedDateTime.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek < Calendar.MONDAY || dayOfWeek > Calendar.FRIDAY) {
            showToast("Appointments can only be scheduled from Monday to Friday.");
            return false;
        }

        // Check if the selected time is between 8 am and 5 pm
        int hourOfDay = selectedDateTime.get(Calendar.HOUR_OF_DAY);
        if (hourOfDay < 8 || hourOfDay >= 17) {
            showToast("Appointments can only be scheduled between 8 am and 5 pm.");
            return false;
        }

        // Check service-specific availability restrictions
        String selectedService = servicesSpinner.getSelectedItem().toString();
        if (!isServiceAvailableOnSelectedDay(selectedService, dayOfWeek)) {
            showToast(getServiceAvailabilityMessage(selectedService));
            return false;
        }

        return true;
    }

    private boolean isServiceAvailableOnSelectedDay(String selectedService, int dayOfWeek) {
        if (selectedService.equals("Vaccination") && dayOfWeek != Calendar.WEDNESDAY) {
            return false; // Vaccination service is only available on Wednesdays
        } else if (selectedService.equals("Maternal and Child Health Care") && dayOfWeek != Calendar.TUESDAY) {
            return false; // Maternal and Child Health Care service is only available on Tuesdays
        }

        return true; // Service is available on the selected day
    }

    private String getServiceAvailabilityMessage(String selectedService) {
        switch (selectedService) {
            case "Vaccination":
                return "Vaccination service is only available on Wednesdays.";
            case "Maternal and Child Health Care":
                return "Maternal and Child Health Care service is only available on Tuesdays.";
            default:
                return "Service is only available on specific days.";
        }
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private ProgressDialog progressDialog;

    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Processing...");
        progressDialog.show();
    }
    private void createAppointment() {
        if (databaseReference == null) {
            Toast.makeText(getActivity(), "Error: Database reference is not initialized.", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }

        String title = servicesSpinner.getSelectedItem().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String date = dateEditText.getText().toString();
        String time = timeEditText.getText().toString();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(date) || TextUtils.isEmpty(time)) {
            Toast.makeText(getActivity(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }

        // Get user details
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Fetch user's name from the database
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            String name = user.getName();
                            String email = user.getEmail();

                            // Set the user's name in the appointment before creating it
                            String key = databaseReference.child("users").child(name).push().getKey();
                            Appointment appointment = new Appointment(key, title, description, date, time, userId, name, "Pending", email);

                            // Fetch the count of appointments for the selected month and year
                            int selectedMonth = selectedDateTime.get(Calendar.MONTH) + 1; // Month is 0-based
                            int selectedYear = selectedDateTime.get(Calendar.YEAR);

                            Query appointmentsCountQuery = databaseReference.child("users")
                                    .child(userId)
                                    .orderByChild("monthYear")
                                    .equalTo(selectedMonth + "-" + selectedYear);

                            appointmentsCountQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    int appointmentsCount = (int) snapshot.getChildrenCount();

                                    if (appointmentsCount >= 80) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(), "Appointment booking for this month is not available. The maximum limit has been reached.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Continue with the appointment creation logic
                                        databaseReference.child("users").child(userId).child(key).setValue(appointment)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        try {
                                                            if (task.isSuccessful()) {


                                                                String appointmentTitle = appointment.getTitle();
                                                                String appointmentKey = appointment.getKey();
                                                                String appointmentDate = appointment.getDate();
                                                                String appointmentTime = appointment.getTime();
                                                                String appointmentStatus = appointment.getStatus();

                                                                scheduleNotification(appointmentKey, appointmentTitle, appointmentDate, appointmentTime, appointmentStatus);


                                                                new Handler().postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        Fragment fragment = new ViewAppointment();
                                                                        getParentFragmentManager().beginTransaction()
                                                                                .replace(R.id.fragment_container, fragment)
                                                                                .addToBackStack(null)
                                                                                .commit();

                                                                        progressDialog.dismiss();
                                                                    }
                                                                }, 2000);
                                                            } else {
                                                                Exception e = task.getException();
                                                                Log.e("CreateAppointment", "Failed to save appointment", e);
                                                                Toast.makeText(getActivity(), "Error saving appointment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                progressDialog.dismiss();
                                                            }
                                                        } finally {
                                                        }
                                                    }
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    progressDialog.dismiss();
                                    Log.e("CheckAppointmentCount", "Error checking appointment count", error.toException());
                                    Toast.makeText(getActivity(), "Error checking appointment count. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    progressDialog.dismiss();
                    Log.e("CreateAppointment", "Error fetching user details", databaseError.toException());
                    Toast.makeText(getActivity(), "Error fetching user details. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    private void scheduleNotification(String appointmentKey, String appointmentTitle, String appointmentDate, String appointmentTime, String appointmentStatus) {
        if ("Confirmed".equals(appointmentStatus)) {
            // Parse the appointment date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            try {
                Date date = dateFormat.parse(appointmentDate + " " + appointmentTime);

                // Calculate the trigger time for the reminder (3 days before the appointment)
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.DAY_OF_MONTH, -3);

                // Create an Intent for the notification
                Intent notificationIntent = new Intent(getActivity(), NotifReceiver.class);
                notificationIntent.setAction("OPEN_NOTIFICATION_FRAGMENT");  // Action to identify in the receiver
                notificationIntent.putExtra("appointmentTitle", appointmentTitle);
                // Add any other data you want to pass to the receiver

                // Create a PendingIntent for the notification
                // Create a PendingIntent for the notification
                PendingIntent pendingIntent;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    pendingIntent = PendingIntent.getBroadcast(
                            getActivity(),
                            0,
                            notificationIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
                    );
                } else {
                    pendingIntent = PendingIntent.getBroadcast(
                            getActivity(),
                            0,
                            notificationIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
                }


                // Schedule the notification with AlarmManager
                AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    } else {
                        // For versions prior to S, use the old flag
                        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                selectedDateTime = Calendar.getInstance();
                selectedDateTime.set(year, monthOfYear, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                dateEditText.setText(sdf.format(selectedDateTime.getTime()));
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (selectedDateTime != null) {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    timeEditText.setText(sdf.format(selectedDateTime.getTime()));
                }
            }
        }, hour, minute, false);

        timePickerDialog.show();
    }
}
