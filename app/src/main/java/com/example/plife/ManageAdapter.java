package com.example.plife;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ManageAdapter extends RecyclerView.Adapter<ManageAdapter.ManageAppointmentViewHolder> {

    private List<Appointment> appointmentList;
    private Context context;
    private static DatabaseReference databaseReference;


    public ManageAdapter(List<Appointment> appointmentList, Context context) {
        this.appointmentList = appointmentList;
        this.context = context;
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public ManageAppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_appointment_list, parent, false);
        return new ManageAppointmentViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ManageAppointmentViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);
        holder.bind(appointment, context);
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointmentList = appointments;
    }

    static class ManageAppointmentViewHolder extends RecyclerView.ViewHolder {

        private TextView textUserName, appointmentTitle, appointmentDate, appointmentTime, appointmentStatus, appointmentDescription;
        private ImageView imageViewMenu;



        public ManageAppointmentViewHolder(@NonNull View itemView) {
            super(itemView);

            textUserName = itemView.findViewById(R.id.textUserName);
            appointmentTitle = itemView.findViewById(R.id.AppointmentTitle);
            appointmentDate = itemView.findViewById(R.id.AppointmentDate);
            appointmentTime = itemView.findViewById(R.id.AppointmentTime);
            appointmentStatus = itemView.findViewById(R.id.AppointmentStatus);
            appointmentDescription = itemView.findViewById(R.id.AppointmentDescription);
            imageViewMenu = itemView.findViewById(R.id.imageViewMenu);
        }

        public void bind(Appointment appointment, Context context) {
            // Set the data to the views
            textUserName.setText(appointment.getName());

            // Set bold text style
            appointmentTitle.setText(Html.fromHtml("<b>Service:</b> " + appointment.getTitle()));
            appointmentDescription.setText(Html.fromHtml("<b>Description:</b> " + appointment.getDescription()));
            appointmentDate.setText(Html.fromHtml("<b>Date:</b> " + appointment.getDate()));
            appointmentTime.setText(Html.fromHtml("<b>Time:</b> " + appointment.getTime()));
            appointmentStatus.setText(Html.fromHtml("<b>Status:</b> " + appointment.getStatus()));

            // Set up the ImageView with a popup menu
            imageViewMenu.setOnClickListener(v -> showPopupMenu(v, appointment, context));
        }


        private void showPopupMenu(View view, Appointment appointment, Context context) {
            // Creating the instance of PopupMenu
            PopupMenu popupMenu = new PopupMenu(context, view);

            // Inflating the Popup using xml file
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

            // Get the menu items
            MenuItem confirmItem = popupMenu.getMenu().findItem(R.id.menu_confirm);
            MenuItem rescheduleItem = popupMenu.getMenu().findItem(R.id.menu_reschedule);
            MenuItem cancelItem = popupMenu.getMenu().findItem(R.id.menu_cancel);
            MenuItem deleteItem = popupMenu.getMenu().findItem(R.id.menu_delete);
            MenuItem markAsDone = popupMenu.getMenu().findItem(R.id.menu_done);

            // Registering popup with OnMenuItemClickListener
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_confirm) {
                    // Handle Confirm Appointment action
                    confirmAppointment(appointment);
                    return true;
                } else if (item.getItemId() == R.id.menu_reschedule) {
                    // Handle Reschedule Appointment action
                    rescheduleAppointment(appointment);
                    return true;
                } else if (item.getItemId() == R.id.menu_done) {
                    // Handle Reschedule Appointment action
                    markAsDone(appointment);
                    return true;
                }
                else if (item.getItemId() == R.id.menu_cancel) {
                    // Handle Cancel Appointment action
                    cancelAppointment(appointment);
                    return true;
                }
                else if (item.getItemId() == R.id.menu_delete) {
                    // Handle Cancel Appointment action
                    DeleteAppointment(appointment);
                    return true;
                } else {
                    return false;
                }
            });

            // Showing the popup menu
            popupMenu.show();
        }

        private void markAsDone(Appointment appointment) {
            markAsDoneStatus(appointment);
            showToast("Appointment marked as done, appointment status updated.");
        }

        private void markAsDoneStatus(Appointment appointment){
            if (databaseReference == null) {
                // Handle the case where the database reference is not initialized
                showToast("Error: Database reference is not initialized.");
                return;
            }

            String userId = appointment.getUserId();
            String appointmentKey = appointment.getKey();

            DatabaseReference appointmentsRef = databaseReference.child("Appointment").child("users").child(userId).child(appointmentKey);

            // Update the appointment status
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("status", "Done");

            appointmentsRef.updateChildren(updateData)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                            } else {
                                Exception e = task.getException();
                                Log.e("UpdateStatus", "Failed to update appointment status", e);
                                Toast.makeText(itemView.getContext(), "Error updating appointment status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }


        private void DeleteAppointment(Appointment appointment) {
            if (databaseReference == null) {
                // Handle the case where the database reference is not initialized
                showToast("Error: Database reference is not initialized.");
                return;
            }

            String userId = appointment.getUserId();
            String appointmentKey = appointment.getKey(); // Use the key obtained during appointment creation

            // Assuming your appointments are stored under the "Appointment" node
            DatabaseReference appointmentsRef = databaseReference.child("Appointment").child("users").child(userId).child(appointmentKey);

            // Remove the appointment from the database
            appointmentsRef.removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                showToast("Appointment deleted successfully.");
                            } else {
                                // Handle the case where deleting the appointment fails
                                Exception e = task.getException();
                                Log.e("DeleteAppointment", "Failed to delete appointment", e);
                                showToast("Error deleting appointment: " + e.getMessage());
                            }
                        }
                    });
        }


        private void confirmAppointment(Appointment appointment) {

            // Update the appointment status in the Firebase Realtime Database
            updateAppointmentStatus(appointment);

            showToast("Appointment Confirmed. Email notification sent.");
        }

        private void updateAppointmentStatus(Appointment appointment) {
            if (databaseReference == null) {
                // Handle the case where the database reference is not initialized
                showToast("Error: Database reference is not initialized.");
                return;
            }

            String userId = appointment.getUserId();
            String appointmentKey = appointment.getKey(); // Use the key obtained during appointment creation

            // Assuming your appointments are stored under the "Appointment" node
            DatabaseReference appointmentsRef = databaseReference.child("Appointment").child("users").child(userId).child(appointmentKey);

            // Update the appointment status to "Confirmed"
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("status", "Confirmed");

            appointmentsRef.updateChildren(updateData)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Send an email to the user about their appointment status
                                sendConfirmationEmail(appointment);
                            } else {
                                // Handle the case where updating the appointment status fails
                                Exception e = task.getException();
                                Log.e("UpdateStatus", "Failed to update appointment status", e);
                                Toast.makeText(itemView.getContext(), "Error updating appointment status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

        private void showToast(String message) {
            // Show a Toast message
            Toast.makeText(itemView.getContext(), message, Toast.LENGTH_SHORT).show();
        }

        private void sendConfirmationEmail(Appointment appointment) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Sender's email address and password
                    final String username = "plife.app.org@gmail.com";
                    final String appPassword = "wxzf hfie fklr hxco";

                    // SMTP server settings
                    Properties props = new Properties();
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.host", "smtp.gmail.com");
                    props.put("mail.smtp.port", "587");

                    // Get the Session object
                    Session session = Session.getInstance(props,
                            new javax.mail.Authenticator() {
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return new PasswordAuthentication(username, appPassword);
                                }
                            });

                    try {
                        // Create a default MimeMessage object
                        Message message = new MimeMessage(session);

                        // Set From: header field of the header
                        message.setFrom(new InternetAddress(username));

                        // Set To: header field of the header
                        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(appointment.getEmail()));

                        // Set Subject: header field
                        message.setSubject("Appointment Confirmation");

                        // Set the actual message
                        message.setText("Dear " + appointment.getName() + ",\n\nYour appointment has been confirmed.\n\nDetails:\n\nService: " + appointment.getTitle() + "\nDescription: " + appointment.getDescription() + "\nDate: " + appointment.getDate() + "\nTime: " + appointment.getTime());


                        // Send message
                        Transport.send(message);

                        // Log success or handle accordingly
                        Log.d("Email", "Email sent successfully");

                    } catch (MessagingException e) {
                        // Log the error or handle accordingly
                        Log.e("Email", "Error sending email", e);
                    }
                }
            }).start();
        }


        private void rescheduleAppointment(Appointment appointment) {
            // Send an email to the user about their appointment status
            recscheduleEmail(appointment);

            // Show toast message
            showToast("Appointment rescheduled, Email notification sent.");
        }

        private void recscheduleEmail(Appointment appointment){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Sender's email address and password
                    final String username = "plife.app.org@gmail.com";
                    final String appPassword = "wxzf hfie fklr hxco";

                    // SMTP server settings
                    Properties props = new Properties();
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.host", "smtp.gmail.com");
                    props.put("mail.smtp.port", "587");

                    // Get the Session object
                    Session session = Session.getInstance(props,
                            new javax.mail.Authenticator() {
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return new PasswordAuthentication(username, appPassword);
                                }
                            });

                    try {
                        // Create a default MimeMessage object
                        Message message = new MimeMessage(session);

                        // Set From: header field of the header
                        message.setFrom(new InternetAddress(username));

                        // Set To: header field of the header
                        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(appointment.getEmail()));

                        // Set Subject: header field
                        message.setSubject("Appointment Reschedule");

                        // Set the actual message
                        message.setText("Dear " + appointment.getName() + ",\n\nPlease reschedule your appointment date and time.\n\n Details:\n\nService: " + appointment.getTitle() + "\nDescription: " + appointment.getDescription() + "\nDate: " + appointment.getDate() + "\nTime: " + appointment.getTime() + "\n\nWe apologize for any inconvenience caused. If you have any questions or concerns, please feel free to contact us.\n\nBest regards,\nPLife App");

                        // Send message
                        Transport.send(message);

                        // Log success or handle accordingly
                        Log.d("Email", "Email sent successfully");

                    } catch (MessagingException e) {
                        // Log the error or handle accordingly
                        Log.e("Email", "Error sending email", e);
                    }
                }
            }).start();
        }

        private void cancelAppointment(Appointment appointment) {
            cancelAppointmentStatus(appointment);

            showToast("Appointment canceled, Email notification sent.");
        }

        private void cancelAppointmentEmail(Appointment appointment){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Sender's email address and password
                    final String username = "plife.app.org@gmail.com";
                    final String appPassword = "wxzf hfie fklr hxco";

                    // SMTP server settings
                    Properties props = new Properties();
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.host", "smtp.gmail.com");
                    props.put("mail.smtp.port", "587");

                    // Get the Session object
                    Session session = Session.getInstance(props,
                            new javax.mail.Authenticator() {
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return new PasswordAuthentication(username, appPassword);
                                }
                            });

                    try {
                        // Create a default MimeMessage object
                        Message message = new MimeMessage(session);

                        // Set From: header field of the header
                        message.setFrom(new InternetAddress(username));

                        // Set To: header field of the header
                        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(appointment.getEmail()));

                        // Set Subject: header field
                        message.setSubject("Appointment Canceled");

                        // Set the actual message
                        message.setText("Dear " + appointment.getName() + ",\n\nYour Appointment has been canceled.\n\n Details:\n\nService: " + appointment.getTitle() + "\nDescription: " + appointment.getDescription() + "\nDate: " + appointment.getDate() + "\nTime: " + appointment.getTime() + "\n\nWe apologize for any inconvenience caused. If you have any questions or concerns, please feel free to contact us.\n\nBest regards,\nPLife App");

                        // Send message
                        Transport.send(message);

                        // Log success or handle accordingly
                        Log.d("Email", "Email sent successfully");

                    } catch (MessagingException e) {
                        // Log the error or handle accordingly
                        Log.e("Email", "Error sending email", e);
                    }
                }
            }).start();
        }

        private void cancelAppointmentStatus(Appointment appointment) {
            if (databaseReference == null) {
                // Handle the case where the database reference is not initialized
                showToast("Error: Database reference is not initialized.");
                return;
            }

            String userId = appointment.getUserId();
            String appointmentKey = appointment.getKey(); // Use the key obtained during appointment creation

            // Assuming your appointments are stored under the "Appointment" node
            DatabaseReference appointmentsRef = databaseReference.child("Appointment").child("users").child(userId).child(appointmentKey);

            // Update the appointment status to "Confirmed"
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("status", "Canceled");

            appointmentsRef.updateChildren(updateData)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Send an email to the user about their appointment status
                                cancelAppointmentEmail(appointment);
                            } else {
                                // Handle the case where updating the appointment status fails
                                Exception e = task.getException();
                                Log.e("UpdateStatus", "Failed to update appointment status", e);
                                Toast.makeText(itemView.getContext(), "Error updating appointment status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }


    }
}
