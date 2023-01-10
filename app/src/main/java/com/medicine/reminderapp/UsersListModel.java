package com.medicine.reminderapp;

public class UsersListModel {

    public String uid,name,phone,email,address;

    public UsersListModel(String uid, String name, String phone, String email, String address) {
        this.uid = uid;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }


    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }
}
