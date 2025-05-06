package com.example.plife;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Find the CardViews by their IDs
        CardView profileCard = view.findViewById(R.id.profile_card);
        CardView appointmentCard = view.findViewById(R.id.appointment_card);
        CardView viewCard = view.findViewById(R.id.view_card);
        CardView lifestyleCard = view.findViewById(R.id.lifestyle_card);


        // Set click listeners for each CardView
        profileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the ProfileFragment
                Fragment profileFragment = new ProfileFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                transaction.replace(R.id.fragment_container, profileFragment);
                transaction.addToBackStack(null); // Optional: Add to back stack for navigation
                transaction.commit();
            }
        });

        appointmentCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the AppointmentFragment or the desired fragment
                Fragment appointmentFragment = new AppointmentFragment(); // Replace with the actual fragment
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                transaction.replace(R.id.fragment_container, appointmentFragment);
                transaction.addToBackStack(null); // Optional: Add to back stack for navigation
                transaction.commit();
            }
        });

        viewCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the ViewAppointmentFragment or the desired fragment
                Fragment viewAppointmentFragment = new ViewAppointment(); // Replace with the actual fragment
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                transaction.replace(R.id.fragment_container, viewAppointmentFragment);
                transaction.addToBackStack(null); // Optional: Add to back stack for navigation
                transaction.commit();
            }
        });

        lifestyleCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the LifestyleFragment or the desired fragment
                Fragment lifestyleFragment = new Chat(); // Replace with the actual fragment
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                transaction.replace(R.id.fragment_container, lifestyleFragment);
                transaction.addToBackStack(null); // Optional: Add to back stack for navigation
                transaction.commit();
            }
        });



        return view;
    }
}
