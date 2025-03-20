package com.onlinestore.jdoulke.onlinestorefx.entities;

public class User {
    private int userId;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private boolean isAdmin;

    public User(int userId, String username, String firstName, String lastName, boolean isAdmin) {
        this.userId = userId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isAdmin = isAdmin;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
