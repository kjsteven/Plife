package com.example.plife;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> implements Filterable {

    private List<User> userList;
    private List<User> originalUserList; // To store the original unfiltered list

    // Constructor to initialize the data
    public UserAdapter(List<User> userList) {
        if (userList == null) {
            this.userList = new ArrayList<>();
        } else {
            this.userList = userList;
        }
        this.originalUserList = new ArrayList<>(this.userList);

        Collections.sort(this.userList, (user1, user2) -> user1.getName().compareToIgnoreCase(user2.getName()));

        this.originalUserList = new ArrayList<>(this.userList);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(userList.get(position));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // ViewHolder class to hold references to the views in each item
    static class UserViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewUserName;
        private final TextView textViewUserEmail;
        private final TextView textViewUserAge;
        private final TextView textViewUserGender;
        private final TextView textViewUserContact;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views by ID
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            textViewUserEmail = itemView.findViewById(R.id.textViewUserEmail);
            textViewUserAge = itemView.findViewById(R.id.textViewUserAge);
            textViewUserGender = itemView.findViewById(R.id.textViewUserGender);
            textViewUserContact = itemView.findViewById(R.id.textViewUserContact);
        }

        // Bind data to the views
        void bind(User user) {
            textViewUserName.setText(user.getName());
            textViewUserEmail.setText(Html.fromHtml("<b>Email:</b> " + user.getEmail()), TextView.BufferType.SPANNABLE);
            textViewUserAge.setText(Html.fromHtml("<b>Age:</b> " + String.valueOf(user.getAge())), TextView.BufferType.SPANNABLE);
            textViewUserGender.setText(Html.fromHtml("<b>Gender:</b> " + user.getGender()), TextView.BufferType.SPANNABLE);
            textViewUserContact.setText(Html.fromHtml("<b>Contact:</b> " + user.getContact()), TextView.BufferType.SPANNABLE);
        }

    }

    // Filterable implementation for searching by email and name
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<User> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(originalUserList);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (User user : originalUserList) {
                        if (user.getEmail().toLowerCase().contains(filterPattern) ||
                                user.getName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(user);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                List<User> filteredList = (List<User>) results.values;

                // Sort the filtered user list alphabetically by name
                Collections.sort(filteredList, (user1, user2) -> user1.getName().compareToIgnoreCase(user2.getName()));

                userList.clear();
                userList.addAll(filteredList);
                notifyDataSetChanged();
            }
        };
    }

    // Method to update the user list with new data
    public void updateUserList(List<User> users) {
        this.userList = new ArrayList<>(users);  // Make a copy of the list
        Collections.sort(this.userList, (user1, user2) -> user1.getName().compareToIgnoreCase(user2.getName()));  // Sort the list
        notifyDataSetChanged();
    }
}
