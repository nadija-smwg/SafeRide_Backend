package com.saferide.SaferideBackend.models;

public class ScheduleItem {
    private String day;
    private String pickupLocation;
    private String dropoffLocation;
    private boolean availabilityToPickup;

    public ScheduleItem() {}

    public ScheduleItem(String day, String pickupLocation, String dropoffLocation, boolean availabilityToPickup) {
        this.day = day;
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
        this.availabilityToPickup = availabilityToPickup;
    }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getDropoffLocation() { return dropoffLocation; }
    public void setDropoffLocation(String dropoffLocation) { this.dropoffLocation = dropoffLocation; }

    public boolean isAvailabilityToPickup() { return availabilityToPickup; }
    public void setAvailabilityToPickup(boolean availabilityToPickup) { this.availabilityToPickup = availabilityToPickup; }
}
