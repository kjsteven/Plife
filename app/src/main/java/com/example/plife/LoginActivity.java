package com.example.plife;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.auth.GetTokenResult;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText loginEmail, loginPassword;
    Button loginButton;
    TextView signupRedirectText;
    TextView forgotPassword;
    FirebaseAuth mAuth;
    boolean passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        mAuth = FirebaseAuth.getInstance();
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.signupRedirectText);
        forgotPassword = findViewById(R.id.forgotPassword);

        loginPassword.setOnTouchListener(new View.OnTouchListener() {
            final int DRAWABLE_RIGHT = 2;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (loginPassword.getRight() - loginPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        togglePasswordVisibility();
                        return true;
                    }
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password;
                email = String.valueOf(loginEmail.getText());
                password = String.valueOf(loginPassword.getText());

                if (TextUtils.isEmpty(email) || !isEmailValid(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        // Check for custom claims (admin status)
                                        user.getIdToken(true)
                                                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                                                        if (task.isSuccessful()) {
                                                            boolean isAdmin = task.getResult().getClaims().get("admin") != null &&
                                                                    (Boolean) task.getResult().getClaims().get("admin");

                                                            if (isAdmin) {
                                                                // Admin user detected, allow login to the dashboard directly
                                                                Toast.makeText(LoginActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(LoginActivity.this, Dashboard.class);
                                                                startActivity(intent);
                                                                finish();
                                                            } else {
                                                                // Regular user, check if user email is verified
                                                                checkUser(user.getUid());
                                                            }
                                                        } else {
                                                            // Handle error
                                                            Toast.makeText(LoginActivity.this, "Error checking admin status: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(LoginActivity.this, "User object is null.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


            }
        });


        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void checkUser(String userId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.getIdToken(true)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            GetTokenResult result = task.getResult();
                            Map<String, Object> claims = result.getClaims();

                            if (claims != null) {
                                // Log claims for debugging
                                Log.d("CustomClaims", "Claims: " + claims.toString());

                                // Check for the "admin" custom claim
                                boolean isAdmin = claims.containsKey("admin") && (Boolean) claims.get("admin");

                                if (isAdmin) {
                                    // Admin user detected, allow login to the dashboard directly
                                    Toast.makeText(LoginActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, Dashboard.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Regular user, check if user email is verified
                                    boolean isEmailVerified = user.isEmailVerified();

                                    if (isEmailVerified) {
                                        // Email is verified for the user
                                        Toast.makeText(LoginActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Email is not verified for the user
                                        Toast.makeText(LoginActivity.this, "Email is not verified. Please check your email.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                // Claims are null
                                Log.e("CustomClaims", "Claims are null");
                            }
                        } else {
                            // Handle error
                            Log.e("CustomClaims", "Error getting ID token: " + task.getException().getMessage());
                            Toast.makeText(LoginActivity.this, "Error getting ID token: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // User is not logged in
            Toast.makeText(LoginActivity.this, "User not logged in.", Toast.LENGTH_SHORT).show();
        }
    }




    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void togglePasswordVisibility() {
        if (passwordVisible) {
            loginPassword.setInputType(129); // InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            loginPassword.setInputType(144); // InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        passwordVisible = !passwordVisible;
    }


}
