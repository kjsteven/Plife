package com.example.plife;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER_MESSAGE = 1;
    private static final int VIEW_TYPE_ADMIN_MESSAGE = 2;




    private List<Message> messages;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;

        if (viewType == VIEW_TYPE_USER_MESSAGE) {
            view = inflater.inflate(R.layout.item_user_message, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.item_admin_message, parent, false);
            return new AdminMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof AdminMessageViewHolder) {
            ((AdminMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return message.isSentByUser() ? VIEW_TYPE_USER_MESSAGE : VIEW_TYPE_ADMIN_MESSAGE;
    }

    private static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewUserMessage;
        private TextView textViewUserTimestamp;

        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUserMessage = itemView.findViewById(R.id.textViewUserMessage);
            textViewUserTimestamp = itemView.findViewById(R.id.textViewMessageTimestamp);
        }

        public void bind(Message message) {
            textViewUserMessage.setText(message.getText());
            textViewUserTimestamp.setText(message.getFormattedTimestamp());

        }

    }

    private static class AdminMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewAdminMessage;
        private TextView textViewAdminTimestamp;

        public AdminMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAdminMessage = itemView.findViewById(R.id.textViewAdminMessage);
            textViewAdminTimestamp = itemView.findViewById(R.id.textViewMessageTimestamp);
        }

        public void bind(Message message) {
            textViewAdminMessage.setText(message.getText());
            textViewAdminTimestamp.setText(message.getFormattedTimestamp());
        }

    }
}
