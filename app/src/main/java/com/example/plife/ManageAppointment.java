package com.example.plife;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManageAppointment extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ManageAdapter manageAdapter;
    private SearchView searchView;
    private List<Appointment> appointmentList;
    private HashMap<String, Integer> monthlyAppointmentsCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_appointment);

        // Initialize RecyclerView and Adapter
        recyclerView = findViewById(R.id.recyclerViewUsers);
        ImageView imageViewBack = findViewById(R.id.imageViewBack);
        ImageView imageViewOption = findViewById(R.id.imageViewOption);

        appointmentList = new ArrayList<>();
        manageAdapter = new ManageAdapter(appointmentList, this);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(manageAdapter);

        // In your onCreate method
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setUpSearchView();


        // Load appointments from Firebase
        loadAppointmentsFromFirebase();

        imageViewBack.setOnClickListener(v -> {
            // Handle click event here
            redirectToDashboard();
        });

        imageViewOption.setOnClickListener(v -> {
            // Create a popup menu
            PopupMenu popupMenu = new PopupMenu(ManageAppointment.this, imageViewOption);
            popupMenu.getMenuInflater().inflate(R.menu.menu_option, popupMenu.getMenu());

            // Set item click listener
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menuViewTotalAppointments) {
                    // View total appointments logic
                    showTotalAppointmentsDialog(appointmentList);
                    return true;
                } else {
                    return false;
                }
            });

            // Show the popup menu
            popupMenu.show();
        });
    }

    private void setUpSearchView() {
        SearchView searchView = findViewById(R.id.searchView);

        // Set the query listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle the query submit if needed
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle the query change (perform search)
                performSearch(newText);
                return true;
            }
        });
    }


    private void performSearch(String query) {
        List<Appointment> filteredList = filterList(appointmentList, query);
        manageAdapter.setAppointments(filteredList);
        manageAdapter.notifyDataSetChanged();
    }


    private List<Appointment> filterList(List<Appointment> appointments, String query) {
        List<Appointment> filteredList = new ArrayList<>();
        for (Appointment appointment : appointments) {
            if (appointment.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(appointment);
            }
        }
        return filteredList;
    }


    private void redirectToDashboard() {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
    }

    private boolean isUserLoggedIn() {
        // Check if the current user is not null
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    private void showTotalAppointmentsDialog(List<Appointment> appointments) {
        // Create a dialog to display total appointments per month
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Total Appointments");

        // Calculate the total appointments per month
        calculateMonthlyAppointments(appointments);

        // Convert the calculated data into a string to display in the dialog
        StringBuilder dialogMessage = new StringBuilder();

        for (Map.Entry<String, Integer> entry : monthlyAppointmentsCount.entrySet()) {
            String monthYearKey = entry.getKey();
            int count = entry.getValue();

            // Append the month and total appointments information
            dialogMessage.append(monthYearKey).append("\nTotal number of appointments: ").append(count).append("\n\n");
        }

        builder.setMessage(dialogMessage.toString());

        // Add an OK button
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing or handle the OK button click
            }
        });

        // Show the dialog
        builder.show();
    }


    private String getMonthYearKey(String dateString) {
        try {
            // Parse the date string to a Date object
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);

            // Format the date to get a key in the format "Month Year"
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Unknown Date";
        }
    }

    private void calculateMonthlyAppointments(List<Appointment> appointments) {
        // HashMap to store the count of appointments for each month
        monthlyAppointmentsCount = new HashMap<>();

        // Iterate through the list of appointments
        for (Appointment appointment : appointments) {
            // Extract the month from the appointment date
            String appointmentDate = appointment.getDate();
            String monthYearKey = getMonthYearKey(appointmentDate);

            // Update the count for the corresponding month in the HashMap
            int count = monthlyAppointmentsCount.getOrDefault(monthYearKey, 0);
            monthlyAppointmentsCount.put(monthYearKey, count + 1);
        }
    }


    private void loadAppointmentsFromFirebase() {
        // Reference to the appointments in the database
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointment").child("users");

        // Attach a listener to read the data at the appointments reference
        appointmentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Log the entire dataSnapshot
                Log.d("Firebase", "DataSnapshot: " + dataSnapshot);

                // Clear the list before adding new appointments
                appointmentList.clear();

                // Iterate through each user's appointments in the dataSnapshot
                for (DataSnapshot userAppointmentSnapshot : dataSnapshot.getChildren()) {
                    // Log the userAppointmentSnapshot
                    Log.d("Firebase", "UserAppointmentSnapshot: " + userAppointmentSnapshot);

                    // Iterate through each appointment under the user
                    for (DataSnapshot appointmentSnapshot : userAppointmentSnapshot.getChildren()) {
                        // Log the appointmentSnapshot
                        Log.d("Firebase", "AppointmentSnapshot: " + appointmentSnapshot);

                        // Extract the appointment details directly from the appointmentSnapshot
                        Appointment appointment = appointmentSnapshot.getValue(Appointment.class);

                        Log.d("Firebase", "Appointment: " + appointment);

                        if (appointment != null) {
                            // Add the appointment to the list
                            appointmentList.add(appointment);
                        }
                    }
                }

                // Notify the adapter that the data set has changed
                manageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (isUserLoggedIn()) {
                    // Handle errors
                    Toast.makeText(ManageAppointment.this, "Error fetching users appointments", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}