package com.example.lucincin;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileRequest {
    private String name;
    private String address;

    @SerializedName("avatar_url")
    private String avatarUrl;

    private String description;
    private String phone;

    public UpdateProfileRequest(String name, String address, String avatarUrl, String description, String phone) {
        this.name = name;
        this.address = address;
        this.avatarUrl = avatarUrl;
        this.description = description;
        this.phone = phone;
    }


}
