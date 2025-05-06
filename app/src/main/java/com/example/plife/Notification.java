package com.example.plife;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Notification extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private TextView noAppointmentsTextView;
    private DatabaseReference appointmentsRef;
    private ValueEventListener appointmentsListener;
    private BroadcastReceiver notificationReceiver;

    public Notification() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        // Initialize UI elements
        recyclerView = view.findViewById(R.id.recyclerView);
        noAppointmentsTextView = view.findViewById(R.id.noAppointmentsTextView);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Initialize Firebase
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            appointmentsRef = FirebaseDatabase.getInstance().getReference()
                    .child("Appointment")
                    .child("users")
                    .child(currentUser.getUid());

            // Listen for changes in the appointments data
            appointmentsListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<Appointment> appointments = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Assuming Appointment is your data model class
                        Appointment appointment = snapshot.getValue(Appointment.class);

                        // Check if the appointment is confirmed before adding to the list
                        if (appointment != null && "Confirmed".equals(appointment.getStatus())) {
                            appointments.add(appointment);
                        }
                    }
                    updateUI(appointments);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            };

            // Attach the listener
            appointmentsRef.addValueEventListener(appointmentsListener);

            // Register a BroadcastReceiver to handle the notification action
            notificationReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // Handle the broadcast action and perform the necessary actions
                    String action = intent.getAction();
                    if ("OPEN_NOTIFICATION_FRAGMENT".equals(action)) {
                        // Handle the action, e.g., refresh the data, navigate, etc.
                        // You may want to extract additional data from the intent if needed
                        refreshData();
                    }
                }
            };

            // Register the BroadcastReceiver
            IntentFilter filter = new IntentFilter("OPEN_NOTIFICATION_FRAGMENT");
            getActivity().registerReceiver(notificationReceiver, filter);
        }

        return view;
    }

    // Update UI based on the availability of upcoming appointments
    private void updateUI(List<Appointment> upcomingAppointments) {
        if (upcomingAppointments != null && !upcomingAppointments.isEmpty()) {
            recyclerView.setVisibility(View.VISIBLE);
            noAppointmentsTextView.setVisibility(View.GONE);
            adapter.updateData(upcomingAppointments);
        } else {
            recyclerView.setVisibility(View.GONE);
            noAppointmentsTextView.setVisibility(View.VISIBLE);
        }
    }

    // Method to refresh data (you can customize this based on your needs)
    private void refreshData() {
        // Example: Refresh the data or perform any necessary actions
        // For simplicity, this example simply re-fetches the data from Firebase
        if (appointmentsRef != null && appointmentsListener != null) {
            appointmentsRef.removeEventListener(appointmentsListener);
            appointmentsRef.addValueEventListener(appointmentsListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove the ValueEventListener and BroadcastReceiver when the fragment is destroyed
        if (appointmentsRef != null && appointmentsListener != null) {
            appointmentsRef.removeEventListener(appointmentsListener);
        }
        if (notificationReceiver != null) {
            getActivity().unregisterReceiver(notificationReceiver);
        }
    }
}
