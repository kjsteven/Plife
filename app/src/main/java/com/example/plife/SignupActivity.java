package com.example.plife;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SignupActivity extends AppCompatActivity {

    EditText signupName, signupEmail, signupPassword, signupContact,signupGender;
    TextView loginRedirectText;
    Button signupButton;

    TextView termsConditionTextView;
    FirebaseAuth mAuth;

    private int year, month, day;
    private EditText birthdateEditText;

    boolean passwordVisible = false;

    @Override
    public void onStart() {
        super.onStart();

        // Check if the user is signed in (non-null) and update the UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void showDatePickerDialog(View view) {
        // Get the current date as default in the date picker
        final java.util.Calendar c = java.util.Calendar.getInstance();
        year = c.get(java.util.Calendar.YEAR);
        month = c.get(java.util.Calendar.MONTH);
        day = c.get(java.util.Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                // Display the selected date in the birthdate EditText
                String selectedDate = (monthOfYear + 1) + "/" + dayOfMonth + "/" + year;
                birthdateEditText.setText(selectedDate);
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupContact = findViewById(R.id.signup_contact);
        birthdateEditText = findViewById(R.id.signup_age);
        signupGender = findViewById(R.id.signup_gender);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        signupButton = findViewById(R.id.signup_button);
        termsConditionTextView = findViewById(R.id.terms_condition);

        // Toggle password visibility when the eye icon is clicked
        signupPassword.setOnTouchListener(new View.OnTouchListener() {
            final int DRAWABLE_RIGHT = 2;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (signupPassword.getRight() - signupPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        togglePasswordVisibility();
                        return true;
                    }
                }
                return false;
            }
        });

        SpannableString spannableString = new SpannableString("By signing up you agree to our Terms and Conditions");

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // Define the URL of your Terms and Conditions page
                String url = "https://sites.google.com/view/plife-privacy-policy/home";

                // Create an Intent to open a web browser
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));

                // Start the activity to open the web browser
                startActivity(intent);
            }
        };

        int startIndex = spannableString.toString().indexOf("Terms and Conditions");

        // Add an UnderlineSpan to the "Terms and Conditions" part
        spannableString.setSpan(new UnderlineSpan(), startIndex, startIndex + "Terms and Conditions".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the ClickableSpan to the specific part of the SpannableString
        spannableString.setSpan(clickableSpan, startIndex, startIndex + "Terms and Conditions".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Apply the SpannableString to the TextView
        termsConditionTextView.setText(spannableString);

        // Enable clickable links in the TextView
        termsConditionTextView.setMovementMethod(LinkMovementMethod.getInstance());

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password, name, contact, birthdate, gender;
                email = String.valueOf(signupEmail.getText());
                password = String.valueOf(signupPassword.getText());
                name = String.valueOf(signupName.getText());
                contact = String.valueOf(signupContact.getText());
                birthdate = String.valueOf(birthdateEditText.getText());
                gender = String.valueOf(signupGender.getText());

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name) || TextUtils.isEmpty(contact) || TextUtils.isEmpty(birthdate) || TextUtils.isEmpty(gender)) {
                    Toast.makeText(SignupActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate the contact number
                if (contact.length() != 11) {
                    Toast.makeText(SignupActivity.this, "Please enter a valid 11-digit contact number.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Calculate age from the birthdate
                int age = calculateAge(birthdate);

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Send email verification
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        user.sendEmailVerification()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            // Email verification sent
                                                            Toast.makeText(SignupActivity.this, "Check your email for a link to verify your account.", Toast.LENGTH_LONG).show();

                                                            // Save user data to Firebase after email verification
                                                            saveUserDataToFirebase(name, email, contact, age, gender);

                                                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                                            startActivity(intent);

                                                        } else {
                                                            // Error sending email verification
                                                            Toast.makeText(SignupActivity.this, "Error sending email verification", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                } else {
                                    if (task.getException() != null) {
                                        String errorMessage = task.getException().getMessage();
                                        if (errorMessage.contains("email address is already in use")) {
                                            // Handle the case where the email is already in use
                                            Toast.makeText(SignupActivity.this, "Email address is already in use.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // Handle other authentication failures
                                            Toast.makeText(SignupActivity.this, "Authentication failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }
                        });


            }
        });

        signupGender.setFocusable(false);
        signupGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGenderDialog();
            }
        });

    }
    // Function to display a dialog for selecting gender
    private void showGenderDialog() {
        final String[] genderOptions = {"Male", "Female"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Gender");
        builder.setItems(genderOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedGender = genderOptions[which];
                signupGender.setText(selectedGender);
                dialog.dismiss();
            }
        });
        builder.show();
    }


    private int calculateAge(String birthdate) {
        // Parse the birthdate string into a Date object
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        Date birthDate = null;
        try {
            birthDate = dateFormat.parse(birthdate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Calculate the age based on the birthdate
        if (birthDate != null) {
            java.util.Calendar dob = java.util.Calendar.getInstance();
            java.util.Calendar today = java.util.Calendar.getInstance();

            dob.setTime(birthDate);
            int age = today.get(java.util.Calendar.YEAR) - dob.get(java.util.Calendar.YEAR);

            if (today.get(java.util.Calendar.DAY_OF_YEAR) < dob.get(java.util.Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age;
        } else {
            return 0; // Error in parsing the birthdate
        }
    }

    private void saveUserDataToFirebase(String name, String email, String contact, int age, String gender) {
        // Get the current user's UID
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String userId = firebaseUser.getUid();

        // Create a reference to the "users" node in your Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Save the user data to the Firebase Realtime Database under the user's UID
        databaseReference.child(userId).child("id").setValue(userId); // Set the "id" field
        databaseReference.child(userId).child("name").setValue(name);
        databaseReference.child(userId).child("email").setValue(email);
        databaseReference.child(userId).child("contact").setValue(contact);
        databaseReference.child(userId).child("age").setValue(age);
        databaseReference.child(userId).child("gender").setValue(gender);
    }


    private void togglePasswordVisibility() {
        if (passwordVisible) {
            signupPassword.setInputType(129); // InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            signupPassword.setInputType(144); // InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        passwordVisible = !passwordVisible;
    }
}
