package com.saferide.SaferideBackend.dto;

public class DriverModeRequest {
    private String driverId;
    private String routeId;
    private boolean enabled;

    public DriverModeRequest() {}

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
