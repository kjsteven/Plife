package com.example.plife;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewAppointment extends Fragment {

    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;
    private DatabaseReference appointmentsReference;
    private String userId;

    // Define the list to store appointments
    private List<Appointment> appointments = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        appointmentsReference = FirebaseDatabase.getInstance().getReference().child("Appointment").child("users").child(userId);

        adapter = new AppointmentAdapter(getContext(), appointmentsReference, userId);
        recyclerView.setAdapter(adapter);

        retrieveData();

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove the ValueEventListener when the fragment is stopped
        if (adapter != null) {
            appointmentsReference.removeEventListener(adapter.getEventListener());
        }
    }

    private void retrieveData() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // User is authenticated, proceed with retrieving data

            // Create a local variable for appointments
            List<Appointment> localAppointments = new ArrayList<>();

            // Retrieve data from Firebase Realtime Database
            appointmentsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int appointmentCount = 0; // To count the number of appointments received

                    for (DataSnapshot appointmentSnapshot : dataSnapshot.getChildren()) {
                        if (appointmentSnapshot != null) {
                            Appointment appointment = appointmentSnapshot.getValue(Appointment.class);
                            if (appointment != null) {
                                String appointmentUserId = appointment.getUserId();
                                if (appointmentUserId != null && appointmentUserId.equals(userId)) {
                                    localAppointments.add(appointment); // Add the appointment to the local list
                                    appointmentCount++;
                                    Log.d("ViewAppointment", "Received appointment: " + appointment.getTitle());
                                }
                            }
                        }
                    }

                    Log.d("ViewAppointment", "Total appointments received: " + appointmentCount);

                    // Update the main "appointments" list and notify the adapter
                    appointments.clear();
                    appointments.addAll(localAppointments);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle any database error
                    Log.e("ViewAppointment", "Database error: " + databaseError.getMessage());
                }
            });
        } else {
            showLoginPrompt();
        }
    }

    private void showLoginPrompt() {
        Snackbar.make(recyclerView, "Please log in to view appointments", Snackbar.LENGTH_INDEFINITE)
                .setAction("Log In", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Handle the click on the "Log In" action
                        // Navigate to the LoginActivity
                        startActivity(new Intent(getContext(), LoginActivity.class));
                    }
                })
                .show();
    }


}
