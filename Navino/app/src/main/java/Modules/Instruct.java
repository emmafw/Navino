package Modules;


import com.google.android.gms.maps.model.LatLng;


public class Instruct {

        public String direction;
    public LatLng point;
    public Distance D;

    public Instruct(String text, LatLng value, Distance D) {
        this.direction = text;
        this.point = value;
        this.D = D;
    }

    public String getDirection()
    {
        return direction;
    }

}
