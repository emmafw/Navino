package com.example.emma.navino.Modules;

import java.util.List;


public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);

    void onDirectionStringSuccess(String navDir);
}
