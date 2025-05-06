package com.example.plife;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageChat extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerViewUsers;
    private AdminChatAdapter adminChatAdapter;
    private List<User> userList;
    private List<User> originalUserList;
    private DatabaseReference databaseReference;
    private ProgressBar progressBar;

    private ChildEventListener childEventListener;

    private DatabaseReference usersRef;

    private boolean isActivityActive = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_chat);

        // Find views by ID
        searchView = findViewById(R.id.searchView);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        progressBar = findViewById(R.id.progressBar);
        ImageView imageViewBack = findViewById(R.id.imageViewBack);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize user list
        userList = new ArrayList<>();
        originalUserList = new ArrayList<>();

        // Set up RecyclerView
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        // Create an instance of AdminChatAdapter and set it to the RecyclerView
        adminChatAdapter = new AdminChatAdapter(this, userList, new AdminChatAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
                ManageChat.this.onItemClick(user);
            }
        });
        recyclerViewUsers.setAdapter(adminChatAdapter);

        // Fetch user data from the Realtime Database
        fetchUserListFromFirebase();
        isActivityActive = true;

        // Set up SearchView listener
        setUpSearchView();

        imageViewBack.setOnClickListener(v -> {
            // Handle click event here
            redirectToDashboard();
        });
    }


    public void onItemClick(User user) {
        // Start AdminChatActivity instead of replacing fragment
        Intent intent = new Intent(this, AdminChat.class);
        intent.putExtra("userId", user.getId()); // Pass the selected user's ID
        startActivity(intent);
    }


    private void setUpSearchView() {
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
        List<User> filteredList = filterList(originalUserList, query);
        adminChatAdapter.updateUserList(filteredList);
    }

    private List<User> filterList(List<User> users, String query) {
        List<User> filteredList = new ArrayList<>();
        for (User user : users) {
            if (user.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }
        return filteredList;
    }
    private void redirectToDashboard() {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
    }

    private void fetchUserListFromFirebase() {
        progressBar.setVisibility(View.VISIBLE); // Show progress bar

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String adminId = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("users");

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // This method will be called once with the results
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null && !user.getId().equals(adminId)) {
                            userList.add(user);
                            originalUserList.add(user); // Add to the original list
                            adminChatAdapter.notifyItemInserted(userList.size() - 1);
                        }
                    }

                    // Hide progress bar when data retrieval is complete
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if (!isFinishing() && isActivityActive) {
                        // Handle database errors
                        Log.d(TAG, "onCancelled: triggered ManageChat");
                        Toast.makeText(ManageChat.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                    }

                    // Always hide progress bar, even in case of error
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }




    @Override
    protected void onStop() {
        super.onStop();
        isActivityActive = false;
        if (usersRef != null && childEventListener != null) {
            usersRef.removeEventListener(childEventListener);
            childEventListener = null;
        }
    }

}
