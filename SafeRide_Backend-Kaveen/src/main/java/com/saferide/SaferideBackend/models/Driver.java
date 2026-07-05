package com.saferide.SaferideBackend.models;

public class Driver {
    private String id;
    private String email;
    private double latitude;
    private double longitude;
    private String fullName;
    private String phoneNumber;
    private String licenseNumber;
    private String vehicleNumber;
    private String profileImageBase64;
    private String activeSessionMode; // null, "MORNING", or "AFTERNOON"

    public Driver() {}

    public String getActiveSessionMode() { return activeSessionMode; }
    public void setActiveSessionMode(String activeSessionMode) { this.activeSessionMode = activeSessionMode; }

    public String getProfileImageBase64() { return profileImageBase64; }
    public void setProfileImageBase64(String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
}
