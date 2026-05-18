package com.example.lucincin;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PostsResponse {
    @SerializedName("status")
    private String status;
    @SerializedName("count")
    private int count;
    @SerializedName("data")
    private List<Post> data;

    public PostsResponse(String status, int count, List<Post> data) {
        this.status = status;
        this.count = count;
        this.data = data;
    }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public int getCount() { return count; }

    public void setCount(int count) { this.count = count; }

    public List<Post> getData() { return data; }

    public void setData(List<Post> data) { this.data = data; }
}
