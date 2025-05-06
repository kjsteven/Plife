package com.example.plife;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;

public class Dashboard extends AppCompatActivity {

    private CardView dbUserCardView;
    private CardView dbAppointmentCardView;
    private CardView dbChatCardView;
    private CardView dbLogoutCardView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbUserCardView = findViewById(R.id.db_user_cardview);
        dbAppointmentCardView = findViewById(R.id.db_appointment_cardview);
        dbChatCardView = findViewById(R.id.db_chat_cardview);
        dbLogoutCardView = findViewById(R.id.db_logout_cardview);

        // Set up click listeners for the CardViews
        setCardViewClickListener(dbUserCardView, ManageUser.class);
        setCardViewClickListener(dbAppointmentCardView, ManageAppointment.class);
        setCardViewClickListener(dbChatCardView, ManageChat.class);
        setLogoutCardViewClickListener(dbLogoutCardView);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity(); // Exit the entire app
    }


    private void setCardViewClickListener(final CardView cardView, final Class<?> destinationActivity) {
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateClick(cardView);
                Intent intent = new Intent(Dashboard.this, destinationActivity);
                startActivity(intent);
            }
        });
    }

    private void setLogoutCardViewClickListener(CardView cardView) {
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateClick(cardView);
                // Log out the current user (admin)
                FirebaseAuth.getInstance().signOut();

                // Redirect to the login activity
                Intent intent = new Intent(Dashboard.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Finish the current activity to prevent going back to the dashboard
            }
        });
    }


    private void animateClick(View view) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 0.9f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 0.9f);
        scaleDownX.setDuration(200);
        scaleDownY.setDuration(200);

        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f);
        scaleUpX.setDuration(200);
        scaleUpY.setDuration(200);

        scaleUpX.setStartDelay(200);
        scaleUpY.setStartDelay(200);

        scaleDownX.start();
        scaleDownY.start();
        scaleUpX.start();
        scaleUpY.start();
    }
}

