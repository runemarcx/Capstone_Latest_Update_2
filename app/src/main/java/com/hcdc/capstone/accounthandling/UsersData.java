package com.hcdc.capstone.accounthandling;

public class UsersData {

    String Barangay, Uid, email, name;
    Boolean isApproved;
    int userpoints;

    public UsersData(){
        //123123
    }

    public UsersData(String barangay, String uid, String email, String name, Boolean isApproved, int userpoints) {
        Barangay = barangay;
        Uid = uid;
        this.email = email;
        this.name = name;
        this.isApproved = isApproved;
        this.userpoints = userpoints;
    }

    public String getBarangay() {
        return Barangay;
    }

    public void setBarangay(String barangay) {
        Barangay = barangay;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getApproved() {
        return isApproved;
    }

    public void setApproved(Boolean approved) {
        isApproved = approved;
    }

    public int getUserpoints() {
        return userpoints;
    }

    public void setUserpoints(int userpoints) {
        this.userpoints = userpoints;
    }
}
