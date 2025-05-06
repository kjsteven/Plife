package com.example.plife;

import com.google.firebase.database.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.Date;

@IgnoreExtraProperties
public class Message {

    private String text;
    private String senderId;
    private long timestamp;
    private boolean sentByUser;
    private String messageId;

    // Default, no-argument constructor required for Firebase
    public Message() {
        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
    }

    public Message(String text, String senderId, String messageId, long timestamp, boolean sentByUser) {
        this.text = text;
        this.senderId = senderId;
        this.messageId = messageId;
        this.timestamp = timestamp;
        this.sentByUser = sentByUser;
    }

    public Message(String text, String senderId, String messageId, long timestamp) {
        this.text = text;
        this.senderId = senderId;
        this.messageId = messageId;
        this.timestamp = timestamp;
        this.sentByUser = false; // Set to false for admin messages
    }


    public String getMessageId() {
        return messageId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSentByUser() {
        return sentByUser;
    }

    public void setSentByUser(boolean sentByUser) {
        this.sentByUser = sentByUser;
    }

    public String getFormattedTimestamp() {
        return dateFormat.format(new Date(timestamp));
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
}
