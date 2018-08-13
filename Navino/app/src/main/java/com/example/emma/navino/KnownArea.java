package com.example.emma.navino;

//class to make saving bounds to Firebase database simpler

public class KnownArea {
    public double nLat, nLon, sLat, sLon;

    public KnownArea(){

    }

    public KnownArea(double nLat, double nLon, double sLat, double sLon){
        this.nLat = nLat;
        this.nLon = nLon;
        this.sLat = sLat;
        this.sLon = sLon;
    }
}
