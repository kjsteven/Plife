package com.example.plife;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageUser extends AppCompatActivity {

    private SearchView searchViewUser;
    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private List<User> userList;
    private List<User> filteredUserList;
    private DatabaseReference databaseReference;

    private ValueEventListener valueEventListener;
    private boolean isActivityActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user);

        // Find views by ID
        searchViewUser = findViewById(R.id.searchViewUser);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        ImageView imageViewBack = findViewById(R.id.imageViewBack);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize user list
        userList = new ArrayList<>();
        filteredUserList = new ArrayList<>();

        // Set up RecyclerView
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        // Create an instance of UserAdapter and set it to the RecyclerView
        userAdapter = new UserAdapter(userList);
        recyclerViewUsers.setAdapter(userAdapter);


        // Fetch user data from the Realtime Database
        fetchUserData();
        isActivityActive = true;

        imageViewBack.setOnClickListener(v -> {
            // Handle click event here
            redirectToDashboard();
        });


        searchViewUser.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle the submit action if needed
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter the user list based on the search query
                filterUserList(newText);
                return true;
            }
        });
    }




    private void redirectToDashboard() {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
    }

    private void fetchUserData() {
        // Create the listener
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isActivityActive) {
                    // Only update the UI if the activity is still active
                    // Clear the existing userList
                    userList.clear();

                    // Iterate through the dataSnapshot and add users to the list
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            userList.add(user);
                        }
                    }

                    // Update the filtered list when user data changes
                    filterUserList(searchViewUser.getQuery().toString());

                    // Notify the adapter that the data has changed
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (isActivityActive && isUserLoggedIn()) {
                    // Handle errors
                    Toast.makeText(ManageUser.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                }
            }
        };

        // Attach the listener to the database reference
        databaseReference.addValueEventListener(valueEventListener);
    }

    private boolean isUserLoggedIn() {
        // Check if the current user is not null
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityActive = false;
        databaseReference.removeEventListener(valueEventListener); // Remove listener
    }


    private void filterUserList(String query) {
        // Clear the filteredUserList
        filteredUserList.clear();


        // If the search query is not empty, find the matching users
        if (!TextUtils.isEmpty(query)) {
            for (User user : userList) {
                if (user.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredUserList.add(user);
                }
            }
        } else {
            // If the search query is empty, add all users to the filtered list
            filteredUserList.addAll(userList);
        }

        // Update the adapter with the filtered list
        userAdapter.updateUserList(filteredUserList);
    }
}