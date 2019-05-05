package com.namseoul.sa.tab;

public class InfoClass {

    public int _id;
    public String name;
    public double latitude;
    public double longitude;
    public int range;

    public InfoClass(){}

    public InfoClass(int _id, String name, double latitude, double longitude, int range){
        this._id = _id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.range = range;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }
}
