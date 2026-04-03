package com.saferide.SaferideBackend.model;

import java.util.Date;

public class TripSession {
    private String tripId;
    private String driverId;
    private String routeId;
    private Date startTime;
    private Date endTime;
    private String status; // ONGOING, COMPLETED
    private Double currentLat;
    private Double currentLong;
    private Date lastUpdateTime;

    public TripSession() {}

    public TripSession(String tripId, String driverId, String routeId) {
        this.tripId = tripId;
        this.driverId = driverId;
        this.routeId = routeId;
        this.startTime = new Date();
        this.status = "ONGOING";
        this.lastUpdateTime = new Date();
    }

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getCurrentLat() { return currentLat; }
    public void setCurrentLat(Double currentLat) { this.currentLat = currentLat; }

    public Double getCurrentLong() { return currentLong; }
    public void setCurrentLong(Double currentLong) { this.currentLong = currentLong; }

    public Date getLastUpdateTime() { return lastUpdateTime; }
    public void setLastUpdateTime(Date lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
}
