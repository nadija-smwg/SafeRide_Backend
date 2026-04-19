package com.saferide.SaferideBackend.dto;

public class StudentRequest {
    private String fullName;
    private String schoolName;
    private int age;

    private double homeLat;
    private double homeLng;
    private double schoolLat;
    private double schoolLng;
    private String homeAddress;
    private String schoolAddress;

    public StudentRequest() {}

    public double getHomeLat() { return homeLat; }
    public void setHomeLat(double homeLat) { this.homeLat = homeLat; }
    public double getHomeLng() { return homeLng; }
    public void setHomeLng(double homeLng) { this.homeLng = homeLng; }
    public double getSchoolLat() { return schoolLat; }
    public void setSchoolLat(double schoolLat) { this.schoolLat = schoolLat; }
    public double getSchoolLng() { return schoolLng; }
    public void setSchoolLng(double schoolLng) { this.schoolLng = schoolLng; }
    public String getHomeAddress() { return homeAddress; }
    public void setHomeAddress(String homeAddress) { this.homeAddress = homeAddress; }
    public String getSchoolAddress() { return schoolAddress; }
    public void setSchoolAddress(String schoolAddress) { this.schoolAddress = schoolAddress; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}
