package com.example.lucincin;

import com.google.gson.annotations.SerializedName;

class Employee {
    @SerializedName("id")
    private String id;

    @SerializedName("employee_name")
    private String name;

    @SerializedName("employee_age")
    private int age;

    @SerializedName("employee_salary")
    private int salary;

    @SerializedName("profile_image")
    private String profileImage;

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public int getSalary() { return salary; }
    public String getProfileImage() { return profileImage; }
}
