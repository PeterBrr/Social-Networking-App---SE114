package com.example.lucincin;

public class ContactItem {
    private String name;
    private String phone;

    public ContactItem(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
}