package com.saferide.SaferideBackend.models;

import java.util.List;

public class Student {
    private String id;
    private String parentId;
    private String fullName;
    private String schoolName;
    private int age;
    private List<ScheduleItem> weeklySchedule;
    private String assignedDriverId;
    private String status = "AT_HOME"; // AT_HOME, IN_TRANSIT, IN_SCHOOL
    private String parentFcmToken;

    private double homeLat;
    private double homeLng;
    private double schoolLat;
    private double schoolLng;
    private String homeAddress;
    private String schoolAddress;

    public Student() {}

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

    public String getAssignedDriverId() { return assignedDriverId; }
    public void setAssignedDriverId(String assignedDriverId) { this.assignedDriverId = assignedDriverId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getParentFcmToken() { return parentFcmToken; }
    public void setParentFcmToken(String parentFcmToken) { this.parentFcmToken = parentFcmToken; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public List<ScheduleItem> getWeeklySchedule() { return weeklySchedule; }
    public void setWeeklySchedule(List<ScheduleItem> weeklySchedule) { this.weeklySchedule = weeklySchedule; }
}
