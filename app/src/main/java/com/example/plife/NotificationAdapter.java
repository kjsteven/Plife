package com.example.plife;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Appointment> appointmentList;

    // Constructor to initialize the dataset
    public NotificationAdapter(List<Appointment> appointmentList) {
        this.appointmentList = appointmentList;
    }

    // Update the dataset when new data is available
    public void updateData(List<Appointment> newData) {
        appointmentList.clear();
        appointmentList.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);

        // Bind data to views with HTML formatting
        holder.titleTextView.setText(Html.fromHtml("<b>Service:</b> " + appointment.getTitle()));
        holder.descriptionTextView.setText(Html.fromHtml("<b>Description:</b> " + appointment.getDescription()));
        holder.dateTextView.setText(Html.fromHtml("<b>Date:</b> " + appointment.getDate()));
        holder.timeTextView.setText(Html.fromHtml("<b>Time:</b> " + appointment.getTime()));

        holder.imageViewMenu.setOnClickListener(v -> showPopupMenu(v, appointment));
    }

    private void showPopupMenu(View view, Appointment appointment) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.appointment_popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_remove_appointment) {
                    // Handle the removal of the appointment here
                    removeAppointment(appointment);
                    return true;
                } else {
                    return false;
                }
            }
        });

        popupMenu.show();
    }


    private void removeAppointment(Appointment appointment) {
        appointmentList.remove(appointment);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    // ViewHolder class to hold references to the views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        TextView dateTextView;
        TextView timeTextView;
        ImageView imageViewMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.textviewTitle);
            descriptionTextView = itemView.findViewById(R.id.textViewDescription);
            dateTextView = itemView.findViewById(R.id.textViewDate);
            timeTextView = itemView.findViewById(R.id.textViewTime);
            imageViewMenu = itemView.findViewById(R.id.imageViewMenu);
        }
    }
}
