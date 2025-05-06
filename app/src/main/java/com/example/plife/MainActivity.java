package com.example.plife;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth auth;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private GetTokenResult idTokenResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        navigationView.setNavigationItemSelectedListener(this);


        if (savedInstanceState == null) {
            // Check the user's role and redirect accordingly
            checkUserRoleAndRedirect();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        } else if (itemId == R.id.nav_profile) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
        } else if (itemId == R.id.nav_appointment) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AppointmentFragment()).commit();
        } else if (itemId == R.id.nav_view) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ViewAppointment()).commit();
        } else if (itemId == R.id.nav_chat) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Chat()).commit();
        } else if (itemId == R.id.nav_notif) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Notification()).commit();
        } else if (itemId == R.id.nav_logout) {
            // Handle logout here
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish(); // Finish MainActivity to prevent going back
        }
        else if (itemId == R.id.nav_delete) {
           deleteAccount();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void deleteAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete your account?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked YES, proceed with account deletion
                dialog.dismiss();
                initiateAccountDeletion();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Cancel, dismiss the dialog
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void initiateAccountDeletion() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Proceed with deletion directly (removing re-authentication)
            proceedWithDeletion(user);
        }
    }

    private void proceedWithDeletion(FirebaseUser user) {
        // 1. Delete user messages from Realtime Database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        databaseRef.child("messages").child("rooms").child(user.getUid()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User messages deleted successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete user messages:", e);
                });

        // 2. Continue with other deletion tasks (user information, profile image, FirebaseAuth account)
        Task<Void> deletionTask = Tasks.whenAllComplete(
                        deleteUserInformationFromDatabase(user.getUid()),
                        deleteUserProfileImage(user.getUid())
                ).continueWithTask(task -> user.delete())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                    navigateToLoginActivity();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete account:", e);
                    Toast.makeText(MainActivity.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                });
    }


    private Task<Void> deleteUserInformationFromDatabase(String userId) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        // Delete user information from Realtime Database
        return databaseRef.child("users").child(userId).removeValue()
                .continueWithTask(task -> databaseRef.child("Appointment").child("users").child(userId).removeValue());
    }

    private Task<Void> deleteUserProfileImage(String userId) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        // Retrieve profile image URL from database
        return databaseRef.child("users").child(userId).child("profileImageURL").get().continueWithTask(task -> {
            String profileImageUrl = (String) task.getResult().getValue();
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                StorageReference profileImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(profileImageUrl);
                // Delete profile image from Firebase Storage
                return profileImageRef.delete();
            } else {
                return Tasks.forResult(null); // No image to delete, return successful task
            }
        });
    }

    private void navigateToLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish(); // Finish MainActivity to prevent going back
    }




    private void checkUserRoleAndRedirect() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.getIdToken(true)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            idTokenResult = task.getResult();
                            Map<String, Object> claims = idTokenResult.getClaims();
                            if (claims != null && claims.containsKey("admin") && (Boolean) claims.get("admin")) {
                                // User has "admin" claim, redirect to Dashboard
                                startActivity(new Intent(MainActivity.this, Dashboard.class));
                                finish();
                            } else {
                                // User doesn't have "admin" claim, redirect to regular user's main activity
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                                navigationView.setCheckedItem(R.id.nav_home);
                            }
                        } else {
                            // Handle error
                            Log.e("MainActivity", "Error fetching user data: " + task.getException().getMessage());
                            Toast.makeText(MainActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MainActivity", "Error fetching user data: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
