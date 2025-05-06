package com.example.plife;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointments = new ArrayList<>();
    private DatabaseReference databaseReference;
    private String currentUserUid;
    private Context context;

    private ValueEventListener valueEventListener;

    public AppointmentAdapter(Context context, DatabaseReference reference, String currentUserUid) {
        this.context = context;
        this.databaseReference = reference;
        this.currentUserUid = currentUserUid;

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                appointments.clear();
                for (DataSnapshot appointmentSnapshot : dataSnapshot.getChildren()) {
                    Appointment appointment = appointmentSnapshot.getValue(Appointment.class);
                    appointments.add(appointment);
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        databaseReference.addValueEventListener(valueEventListener);
    }

    public ValueEventListener getEventListener() {
        return valueEventListener;
    }

    @Override
    public AppointmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AppointmentViewHolder holder, int position) {
        final Appointment appointment = appointments.get(position);

        holder.dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(appointment);
            }
        });

        holder.timeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog(appointment);
            }
        });

        holder.deleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    showPopupMenu(view.getContext(), view, adapterPosition);
                }
            }
        });

        // Set other appointment details
        holder.titleTextView.setText(appointment.getTitle());
        holder.descriptionTextView.setText(appointment.getDescription());
        holder.dateTextView.setText("Date: " + appointment.getDate());
        holder.timeTextView.setText("Time: " + appointment.getTime());
        holder.statusTextView.setText("Status: " + appointment.getStatus());
    }

    private void showDatePickerDialog(final Appointment appointment) {
        EditAppointmentDialog editDialog = new EditAppointmentDialog(context, appointment);
        editDialog.showDatePickerDialog();
    }

    private void showTimePickerDialog(final Appointment appointment) {
        EditAppointmentDialog editDialog = new EditAppointmentDialog(context, appointment);
        editDialog.showTimePickerDialog();
    }

    private void showPopupMenu(Context context, View view, final int adapterPosition) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.delete_menu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.menu_edit_appointment) {
                    // Handle the edit appointment action here
                    editAppointment(appointments.get(adapterPosition));
                    return true;
                } else if (itemId == R.id.menu_delete_appointment) {
                    // Handle the delete appointment action here
                    deleteAppointment(adapterPosition);
                    return true;
                } else {
                    return false;
                }
            }
        });

        popupMenu.show();
    }

    private void editAppointment(Appointment appointment) {
        // Check if the appointment status is "Pending" before allowing the edit
        if ("Pending".equals(appointment.getStatus())) {
            EditAppointmentDialog editDialog = new EditAppointmentDialog(context, appointment);
            editDialog.show();
        } else {
            showToast("You can only edit the appointment if the status is Pending");
        }
    }

    private void deleteAppointment(int position) {
        String key = appointments.get(position).getKey();
        databaseReference.child(key).removeValue();
        appointments.remove(position);
        notifyDataSetChanged();
        showToast("Appointment Deleted");
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView descriptionTextView;
        public TextView dateTextView;
        public TextView timeTextView;
        public TextView statusTextView;
        public ImageView deleteImageView;

        public AppointmentViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textViewTitle);
            descriptionTextView = itemView.findViewById(R.id.textViewDescription);
            dateTextView = itemView.findViewById(R.id.textViewDate);
            timeTextView = itemView.findViewById(R.id.textViewTime);
            statusTextView = itemView.findViewById(R.id.textViewStatus);
            deleteImageView = itemView.findViewById(R.id.deleteAppointment);
        }
    }
}
