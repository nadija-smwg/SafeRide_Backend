package com.saferide.SaferideBackend.models;

public class Parent {
    private String id;
    private String fullName;
    private String phoneNumber;
    private String homeAddress;
    private java.util.List<LocationObject> savedLocations;

    public Parent() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getHomeAddress() { return homeAddress; }
    public void setHomeAddress(String homeAddress) { this.homeAddress = homeAddress; }

    public java.util.List<LocationObject> getSavedLocations() { return savedLocations; }
    public void setSavedLocations(java.util.List<LocationObject> savedLocations) { this.savedLocations = savedLocations; }
}
