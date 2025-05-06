package com.example.plife;

// AdminChat.java
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;

public class AdminChat extends AppCompatActivity {

    private EditText editTextMessage;
    private ImageView buttonSend;
    private RecyclerView recyclerViewChat;
    private MessageAdapter messageAdapter;
    private List<Message> messages;

    // Firebase
    private DatabaseReference userMessagesRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat);

        // Find views by ID
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.adbuttonSend);
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        ImageView imageViewBack = findViewById(R.id.imageViewBack);

        // Initialize RecyclerView and adapter
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(messageAdapter);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String recipientUserId = getIntent().getStringExtra("userId");

            // Admin has access to messages directly under the user ID
            userMessagesRef = FirebaseDatabase.getInstance().getReference("messages").child("rooms").child(recipientUserId);

            // Set up message listener with ordering by timestamp
            userMessagesRef.orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                    Message receivedMessage = dataSnapshot.getValue(Message.class);
                    if (receivedMessage != null) {
                        messages.add(receivedMessage);
                        messageAdapter.notifyDataSetChanged();
                        recyclerViewChat.scrollToPosition(messages.size() - 1);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                    // Handle changed messages if needed
                    Message updatedMessage = dataSnapshot.getValue(Message.class);
                    if (updatedMessage != null) {
                        int index = getMessageIndex(updatedMessage.getMessageId());
                        if (index != -1) {
                            messages.set(index, updatedMessage);
                            messageAdapter.notifyItemChanged(index);
                        }
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    // Handle removed messages if needed
                    Message removedMessage = dataSnapshot.getValue(Message.class);
                    if (removedMessage != null) {
                        int index = getMessageIndex(removedMessage.getMessageId());
                        if (index != -1) {
                            messages.remove(index);
                            messageAdapter.notifyItemRemoved(index);
                        }
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                    // Handle moved messages if needed
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database errors
                    Log.e("AdminChat", "Database error: " + databaseError.getMessage());
                }
            });

            buttonSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String messageText = editTextMessage.getText().toString().trim();
                    if (!messageText.isEmpty()) {
                        String messageId = userMessagesRef.push().getKey();
                        Message newMessage = new Message(messageText, currentUser.getUid(), messageId, System.currentTimeMillis());
                        userMessagesRef.child(messageId).setValue(newMessage);
                        editTextMessage.setText("");
                    }
                }
            });

            imageViewBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish(); // Back button functionality
                }
            });
        }
    }

    private int getMessageIndex(String messageId) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getMessageId().equals(messageId)) {
                return i;
            }
        }
        return -1;
    }
}
