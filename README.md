# Plife - Healthcare Appointment Management App

## Overview

Plife is an Android mobile application designed to streamline healthcare appointment management. The app enables users to schedule medical appointments, manage their profiles, and communicate with healthcare administrators through an integrated chat system.

## Features

### User Management
- User registration and authentication
- Profile management with personal details (name, age, gender, contact information)
- Secure password reset functionality
- Account deletion capability

### Appointment System
- Schedule appointments for various medical services
- Select from multiple healthcare service categories
- Choose specific service descriptions
- Date and time selection with validity checks
- Appointment status tracking (Pending, Confirmed, Canceled, Done)
- Email notifications for appointment confirmations and cancellations

### Admin Features
- User account management through ManageUser interface
- Appointment management (confirm, reschedule, mark as done, cancel)
- Chat with patients/users
- Search functionality for users and appointments

### Notification System
- Appointment reminders
- Status change notifications
- Real-time updates

### Chat System
- Direct communication between users and administrators
- Message history tracking
- Real-time messaging

## Technical Details

### Architecture
- Java-based Android application
- Firebase Authentication for user management
- Firebase Realtime Database for data storage
- Firebase Storage for profile image management

### Dependencies
- Firebase Authentication
- Firebase Realtime Database
- Firebase Storage
- AndroidX libraries
- RecyclerView for list displays
- CardView for UI elements
- Email integration for notifications

## Installation

1. Clone the repository
2. Open the project in Android Studio
3. Connect to your Firebase project
4. Configure google-services.json
5. Build and run the application

## Usage

### For Users
1. Register a new account or log in
2. Navigate to the dashboard to access various features
3. Schedule appointments by selecting services and preferred times
4. View and manage existing appointments
5. Communicate with administrators through the chat feature

### For Administrators
1. Log in with admin credentials
2. Access the admin dashboard
3. Manage user accounts through the User Management screen
4. Handle appointments (confirm, reschedule, cancel) via the Appointment Management screen
5. Communicate with users through the Chat Management interface

## Security Features
- Secure user authentication
- Password protection
- Data encryption
- Safe account management

## Future Enhancements
- Integration with payment gateways
- Medical record management
- Prescription system
- Advanced reporting capabilities
- Multi-language support

