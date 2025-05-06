package com.example.plife;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class ProfileFragment extends Fragment {

    private ImageView profilePic;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private TextView saveChangesTextView;
    private TextView ageTextView;

    private EditText editContactEditText;

    private TextView fullnameTextView;

    private TextView emailTextView;

    private TextView genderTextView;

    private Button saveChangesButton;

    private View rootView;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        profilePic = rootView.findViewById(R.id.profilePic);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        saveChangesTextView = rootView.findViewById(R.id.SaveChanges);
        ageTextView = rootView.findViewById(R.id.textViewAge);
        ageTextView.setEnabled(false);
        genderTextView = rootView.findViewById(R.id.Gender);
        editContactEditText = rootView.findViewById(R.id.textViewContact);
        fullnameTextView = rootView.findViewById(R.id.FullName);

        emailTextView = rootView.findViewById(R.id.textViewEmail);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        // Find the "Save Changes" button and set a click listener
        saveChangesButton = rootView.findViewById(R.id.SaveChanges);
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(requireContext(), rootView);
                updateProfileInformation();
                Toast.makeText(requireContext(), "Profile changes saved.", Toast.LENGTH_SHORT).show();
            }
        });

        // Retrieve and populate profile information
        retrieveProfileInformation();

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicture();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profilePic.setImageURI(imageUri);
            uploadPicture();
        }
    }

    private void uploadPicture() {
        final ProgressDialog pd = new ProgressDialog(requireContext());
        pd.setTitle("Uploading Image...");
        pd.show();

        final String randomKey = UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child("images/" + randomKey);

        riversRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    pd.dismiss();
                    // Get the download URL of the uploaded image
                    riversRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();

                        // Store the image URL in the database
                        String userId = mAuth.getCurrentUser().getUid();
                        DatabaseReference imageRef = databaseReference.child("profileImageURL");
                        imageRef.setValue(imageUrl);
                    });
                    Snackbar.make(profilePic, "Image Uploaded.", Snackbar.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(requireContext(), "Failed to Upload", Toast.LENGTH_LONG).show();
                })
                .addOnProgressListener(snapshot -> {
                    double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    pd.setMessage("Percentage: " + (int) progressPercent + "%");
                });
    }




    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    private void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void retrieveProfileInformation() {
        // Retrieve profile information from the Realtime Database and populate the fields
        databaseReference.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Long age = documentSnapshot.child("age").getValue(Long.class);
                String gender = documentSnapshot.child("gender").getValue(String.class);
                String contact = documentSnapshot.child("contact").getValue(String.class);
                String fullName = documentSnapshot.child("name").getValue(String.class);
                String email = documentSnapshot.child("email").getValue(String.class);
                String imageUrl = documentSnapshot.child("profileImageURL").getValue(String.class);

                // Load and display the profile image using Glide
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.profile_image) // You can use a placeholder image
                            .error(R.drawable.profile_image) // You can use an error image
                            .into(profilePic);
                }

                ageTextView.setText(String.valueOf(age));
                fullnameTextView.setText(fullName);
                emailTextView.setText(email);
                genderTextView.setText(gender);
                editContactEditText.setText(contact);
            }
        });
    }


    private void updateProfileInformation() {
        String userId = mAuth.getCurrentUser().getUid();

        // Update profile information in the Realtime Database
        String contact = editContactEditText.getText().toString();

        databaseReference.child("contact").setValue(contact);
    }
}
