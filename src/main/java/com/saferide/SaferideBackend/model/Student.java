package com.saferide.SaferideBackend.model;

public class Student {
    private String studentId;
    private String studentName;
    private String homeAddress;
    private String schoolAddress;
    private String emergencyContact;
    private String parentEmail;
    private String parentFcmToken;
    private String currentStatus; // AT_HOME, PICKED_UP, DROPPED_OFF

    //Default constructor is crucial for Firebase and JSON parsing!
    //Firebase / Spring / JSON libraries,frameworks like this need this to create objects automatically
    //Without this your app will crash when reading data and parsing JSON API requests
    public Student() {}

    //allow to create objs manually
    //creating student before saving to DB
    public Student(String studentId, String studentName, String homeAddress,
                   String schoolAddress, String emergencyContact, String parentEmail, String parentFcmToken) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.homeAddress = homeAddress;
        this.schoolAddress = schoolAddress;
        this.emergencyContact = emergencyContact;
        this.parentEmail = parentEmail;
        this.parentFcmToken = parentFcmToken; //for push notifications ( FB cloud messaging)
        this.currentStatus = "AT_HOME"; // Initial status
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

    public String getParentFcmToken() { return parentFcmToken; }
    public void setParentFcmToken(String parentFcmToken) { this.parentFcmToken = parentFcmToken; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
}

//QR code contains studentID, backend receives it,fetch student from DB and hold data

