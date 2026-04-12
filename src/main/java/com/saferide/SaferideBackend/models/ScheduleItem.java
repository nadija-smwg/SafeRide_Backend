package com.saferide.SaferideBackend.models;

public class ScheduleItem {
    private String day;
    
    private LocationObject morningPickup;
    private LocationObject morningDropoff;
    private boolean needMorningPickup;
    
    private LocationObject eveningPickup;
    private LocationObject eveningDropoff;
    private boolean needEveningPickup;

    public ScheduleItem() {}

    public ScheduleItem(String day, LocationObject morningPickup, LocationObject morningDropoff, boolean needMorningPickup,
                        LocationObject eveningPickup, LocationObject eveningDropoff, boolean needEveningPickup) {
        this.day = day;
        this.morningPickup = morningPickup;
        this.morningDropoff = morningDropoff;
        this.needMorningPickup = needMorningPickup;
        this.eveningPickup = eveningPickup;
        this.eveningDropoff = eveningDropoff;
        this.needEveningPickup = needEveningPickup;
    }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public LocationObject getMorningPickup() { return morningPickup; }
    public void setMorningPickup(LocationObject morningPickup) { this.morningPickup = morningPickup; }

    public LocationObject getMorningDropoff() { return morningDropoff; }
    public void setMorningDropoff(LocationObject morningDropoff) { this.morningDropoff = morningDropoff; }

    public boolean isNeedMorningPickup() { return needMorningPickup; }
    public void setNeedMorningPickup(boolean needMorningPickup) { this.needMorningPickup = needMorningPickup; }

    public LocationObject getEveningPickup() { return eveningPickup; }
    public void setEveningPickup(LocationObject eveningPickup) { this.eveningPickup = eveningPickup; }

    public LocationObject getEveningDropoff() { return eveningDropoff; }
    public void setEveningDropoff(LocationObject eveningDropoff) { this.eveningDropoff = eveningDropoff; }

    public boolean isNeedEveningPickup() { return needEveningPickup; }
    public void setNeedEveningPickup(boolean needEveningPickup) { this.needEveningPickup = needEveningPickup; }
}
