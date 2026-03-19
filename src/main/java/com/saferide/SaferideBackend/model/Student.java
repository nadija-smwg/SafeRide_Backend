package com.saferide.saferide_backend.model;

public class Student {
    private String studentId;
    private String studentName;
    private String homeAddress;
    private String schoolAddress;
    private String emergencyContact;
    private String parentEmail;

    // Default constructor is crucial for Firebase and JSON parsing!
    public Student() {}

    public Student(String studentId, String studentName, String homeAddress,
                   String schoolAddress, String emergencyContact, String parentEmail) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.homeAddress = homeAddress;
        this.schoolAddress = schoolAddress;
        this.emergencyContact = emergencyContact;
        this.parentEmail = parentEmail;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getHomeAddress() { return homeAddress; }
    public void setHomeAddress(String homeAddress) { this.homeAddress = homeAddress; }

    public String getSchoolAddress() { return schoolAddress; }
    public void setSchoolAddress(String schoolAddress) { this.schoolAddress = schoolAddress; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getParentEmail() { return parentEmail; }
    public void setParentEmail(String parentEmail) { this.parentEmail = parentEmail; }
}
