package com.example.emma.navino.Modules;

//https://github.com/hiepxuan2008/GoogleMapDirectionSimple

import java.util.List;


public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);

    void onDirectionStringSuccess(String navDir);
}
