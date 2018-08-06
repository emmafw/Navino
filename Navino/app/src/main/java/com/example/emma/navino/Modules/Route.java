package com.example.emma.navino.Modules;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Route {
    public Distance distance;
    public Duration duration;
    public String endAddress;
    public List<Instruct> directions;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;
    public Double Elat;
    public Double Elng;
    public List<LatLng> points;
}
