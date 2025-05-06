package com.example.plife;

public class User {
    private String name;
    private String email;
    private int age;
    private String id;
    private String contact;
    private String gender;
    private String profileImageURL;



    // Add a no-argument constructor for Firebase to be able to create an instance of the User class
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String id, String name, String email, int age, String gender, String contact, String profileImageURL) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.contact = contact;
        this.profileImageURL = profileImageURL; // Initialize the profile image URL
        // Initialize other attributes as needed
    }



    // Add getter method for the profile image URL
    public String getProfileImageURL() {
        return profileImageURL;
    }

    // Add setter method for the profile image URL if needed
    public void setProfileImageURL(String profileImageURL) {
        this.profileImageURL = profileImageURL;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public String getContact() {
        return contact;
    }

    public int getAge() {
        return age;
    }
}

