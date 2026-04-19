package com.saferide.SaferideBackend.models;

public class LocationObject {
    private String id;
    private String name;
    private double lat;
    private double lng;
    private String address;

    public LocationObject() {}

    public LocationObject(String id, String name, double lat, double lng, String address) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.address = address;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
