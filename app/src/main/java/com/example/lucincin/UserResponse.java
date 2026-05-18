package com.example.lucincin;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;
    @SerializedName("user")
    private User user;

    public UserResponse(String status, String message, User user) {
        this.status = status;
        this.message = message;
        this.user = user;
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public User getUser() { return user; }

    public void setStatus(String status) { this.status = status; }
    public void setMessage(String message) { this.message = message; }
    public void setUser(User user) { this.user = user; }
}
