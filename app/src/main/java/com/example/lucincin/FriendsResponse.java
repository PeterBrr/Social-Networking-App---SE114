package com.example.lucincin;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FriendsResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("count")
    private int count;

    @SerializedName("friends")
    private List<User> friends;

    // Getters
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public int getCount() { return count; }
    public List<User> getFriends() { return friends; }

    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setMessage(String message) { this.message = message; }
    public void setCount(int count) { this.count = count; }
    public void setFriends(List<User> friends) { this.friends = friends; }
}
