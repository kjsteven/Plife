package com.example.plife;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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

public class Chat extends Fragment {

    private EditText editTextMessage;
    private ImageButton buttonSend;
    private RecyclerView recyclerViewChat;
    private MessageAdapter messageAdapter;
    private List<Message> messages;

    // Firebase
    private DatabaseReference userMessagesRef;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Initialize views
        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);
        recyclerViewChat = view.findViewById(R.id.recyclerViewChat);

        // Initialize RecyclerView and adapter
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewChat.setAdapter(messageAdapter);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Each user has their own room with their own messages
            userMessagesRef = FirebaseDatabase.getInstance().getReference("messages").child("rooms").child(userId);

            // Set up message listener with ordering by timestamp
            userMessagesRef.orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                    Message receivedMessage = dataSnapshot.getValue(Message.class);
                    if (receivedMessage != null) {
                        receivedMessage.setMessageId(dataSnapshot.getKey()); // Set messageId
                        messages.add(receivedMessage);
                        messageAdapter.notifyDataSetChanged();
                        recyclerViewChat.scrollToPosition(messages.size() - 1);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                    // Handle changed messages if needed
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    // Handle removed messages if needed
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                    // Handle moved messages if needed
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database errors
                    Log.e("ChatFragment", "Database error: " + databaseError.getMessage());
                }
            });

            buttonSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String messageText = editTextMessage.getText().toString().trim();
                    if (!messageText.isEmpty()) {
                        // Get a unique messageId (you can generate it based on your requirements)
                        String messageId = userMessagesRef.push().getKey();

                        // Create a new Message with the messageId
                        Message newMessage = new Message(messageText, userId, messageId, System.currentTimeMillis(), true);

                        // Save the newMessage to the userMessagesRef
                        userMessagesRef.child(messageId).setValue(newMessage);

                        editTextMessage.setText("");
                    }
                }
            });
        }

        return view;
    }
}
