package com.example.plife;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminChatAdapter extends RecyclerView.Adapter<AdminChatAdapter.ViewHolder> {

    private Context context;
    private List<User> userList;
    private OnItemClickListener onItemClickListener;
    private List<User> originalUserList;

    public AdminChatAdapter(Context context, List<User> userList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.userList = userList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_chat_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);

        // Check if the user has a profile image URL
        if (user.getProfileImageURL() != null && !user.getProfileImageURL().isEmpty()) {
            // Load user profile picture using Picasso (or your preferred image loading library)
            Picasso.get()
                    .load(user.getProfileImageURL())
                    .resize(60, 60)
                    .centerCrop()
                    .into(holder.profilePic);
        } else {
            // Load default profile picture
            holder.profilePic.setImageResource(R.drawable.smile_icon);
        }

        // Set user name
        holder.textUserName.setText(user.getName());

        // Set click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(user);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateUserList(List<User> filteredUserList) {
        userList.clear();
        userList.addAll(filteredUserList);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profilePic;
        TextView textUserName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.profilePic);
            textUserName = itemView.findViewById(R.id.textUserName);

        }
    }
}
