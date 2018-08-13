package com.example.emma.navino;

//https://github.com/NilaxSpaceo/GoogleMapOverlay
//https://github.com/hiepxuan2008/GoogleMapDirectionSimple

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.emma.navino.Modules.*;
import com.example.emma.navino.view.DrawingPanel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private GoogleMap mMap;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount mUser;
    private PlaceAutocompleteFragment placeAutocompleteFragment;
    private LocationManager locationManager;
    private LinearLayout llMapActionContainer, llSaveClearContainer,
            llMarkerDroppedContainer, llTopPanelContainer,
            llDirectionsFoundContainer, llDirectionsStartedContainer,
            llNavDirContainer;
    private TextView txtDirections;
    private FrameLayout flMapContainer;
    private UiSettings mUiSettings;
    private Marker marker, sourceMarker, destinationMarker;
    private LatLngBounds latLngBounds, bounds;
    private double lat, lon, maxDistanceFromCenter, nLat, nLon, sLat, sLon;
    private String latitude, longitude, text;
    private DrawingPanel drawingPanel;
    private Projection projection;
    private ArrayList<LatLng> latLngs;
    private PolygonOptions polygonOptions;
    private LatLng latLng, latLngSource, latLngDestination;
    private TextToSpeech t1;
    private ProgressDialog progressDialog;
    private ArrayList<Polyline> polylinePaths;
    private ArrayList<Marker> originMarkers, destinationMarkers;
    private int ldiri = 0, diri = 0;
    private List<String> ldir = new ArrayList<String>();
    private List<LatLng> dir = new ArrayList<LatLng>();
    private ImageButton clear;
    private List<LatLngBounds> userBounds = new ArrayList<LatLngBounds>();
    private boolean inKnownArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        inKnownArea = false;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //buildAlertMessageNoGps();
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getLocation();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mUser = GoogleSignIn.getLastSignedInAccount(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        placeAutocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        placeAutocompleteFragment.setFilter(new AutocompleteFilter.Builder().setCountry("USA").build());
        placeAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                LatLng loc = place.getLatLng();
                if (marker != null) {
                    marker.remove();
                }
                marker = mMap.addMarker(new MarkerOptions().position(loc));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 18));
                llMarkerDroppedContainer.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeIn).playOn(llMarkerDroppedContainer);
                llMapActionContainer.setVisibility(View.GONE);
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(MapsActivity.this, "" + status.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        llMapActionContainer = (LinearLayout) findViewById(R.id.llMapActionContainer);
        llSaveClearContainer = (LinearLayout) findViewById(R.id.llSaveOrClearContainer);
        llMarkerDroppedContainer = (LinearLayout) findViewById(R.id.llMarkerDropedContainer);
        llTopPanelContainer = (LinearLayout) findViewById(R.id.topPanel);
        llDirectionsFoundContainer = (LinearLayout) findViewById(R.id.llDirectionsFoundContainer);
        llDirectionsStartedContainer = (LinearLayout) findViewById(R.id.llDirectionsStartedContainer);
        llNavDirContainer = (LinearLayout) findViewById(R.id.llNavDir);
        llTopPanelContainer = (LinearLayout) findViewById(R.id.topPanel);
        flMapContainer = (FrameLayout) findViewById(R.id.flMapContainer);
        txtDirections = (TextView) findViewById(R.id.textDir);
        clear = (ImageButton) findViewById(R.id.place_autocomplete_clear_button);

        txtDirections.setText("");

        drawingPanel = new DrawingPanel(this);
        drawingPanel.setVisibility(View.GONE);
        drawingPanel.setBackgroundColor(Color.TRANSPARENT);
        flMapContainer.addView(drawingPanel);

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.ENGLISH);
                }
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeAutocompleteFragment.setText("");
                mMap.clear();
                getLocation();
                llMarkerDroppedContainer.setVisibility(View.GONE);
                llMapActionContainer.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeIn).playOn(llMapActionContainer);
                LatLng current = new LatLng(lat, lon);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 18));
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setPadding(0, 0, 0, 100);
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.setZoomControlsEnabled(true);
        LatLng current = new LatLng(lat, lon);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 18));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        llMapActionContainer.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.FadeIn).playOn(llMapActionContainer);

        drawingPanel.setOnDragListener(new DrawingPanel.OnDragListener() {
            @Override
            public void onDragEvent(MotionEvent motionEvent) {
                float x = motionEvent.getX();
                float y = motionEvent.getY();

                int x_co = Integer.parseInt(String.valueOf(Math.round(x)));
                int y_co = Integer.parseInt(String.valueOf(Math.round(y)));

                //get Projection from google map
                projection = mMap.getProjection();
                Point x_y_points = new Point(x_co, y_co);
                LatLng latLng = mMap.getProjection().fromScreenLocation(x_y_points);

                if (latLngs == null)
                    latLngs = new ArrayList<LatLng>();
                latLngs.add(latLng);

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                    //Join all points and draw polygon
                    polygonOptions.strokeWidth(10);
                    polygonOptions.strokeColor(Color.BLUE);
                    polygonOptions.addAll(latLngs);
                    mMap.addPolygon(polygonOptions);

                    drawingPanel.setVisibility(View.GONE);


                    //find radius and center from drawing
                    LatLng latLng1 = getPolygonCenterPoint(latLngs);
                    LatLng latLngxmin = projection.fromScreenLocation(drawingPanel.getPointXMin());
                    LatLng latLngxmax = projection.fromScreenLocation(drawingPanel.getPointxMax());
                    LatLng latLngymin = projection.fromScreenLocation(drawingPanel.getPointYmin());
                    LatLng latLngymax = projection.fromScreenLocation(drawingPanel.getPointYmax());


                    if (drawingPanel.getPointXMin().x != 0 && drawingPanel.getPointXMin().y != 0)
                        maxDistanceFromCenter = distance(latLng1.latitude, latLng1.longitude, latLngxmin.latitude, latLngxmin.longitude);


                    double tempdistance = 0;
                    if (drawingPanel.getPointxMax().x != 0 && drawingPanel.getPointxMax().y != 0)
                        tempdistance = distance(latLng1.latitude, latLng1.longitude, latLngxmax.latitude, latLngxmax.longitude);
                    if (tempdistance > maxDistanceFromCenter)
                        maxDistanceFromCenter = tempdistance;

                    if (drawingPanel.getPointYmin().x != 0 && drawingPanel.getPointYmin().y != 0)
                        tempdistance = distance(latLng1.latitude, latLng1.longitude, latLngymin.latitude, latLngymin.longitude);
                    if (tempdistance > maxDistanceFromCenter)
                        maxDistanceFromCenter = tempdistance;

                    if (drawingPanel.getPointYmax().x != 0 && drawingPanel.getPointYmax().y != 0)
                        tempdistance = distance(latLng1.latitude, latLng1.longitude, latLngymax.latitude, latLngymax.longitude);

                    if (tempdistance > maxDistanceFromCenter)
                        maxDistanceFromCenter = tempdistance;
                }
            }
        });
    }

    public void onAddKnownAreaClick(View view) {
        llMapActionContainer.setVisibility(View.GONE);
        if (latLngs != null) {
            latLngs.clear();
        }
        drawingPanel.setVisibility(View.VISIBLE);
        drawingPanel.clear();
        polygonOptions = new PolygonOptions();
        llSaveClearContainer.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.FadeIn).playOn(llSaveClearContainer);
    }

    public void onSignoutClick(View view) {
        mGoogleSignInClient.signOut();
        //finish implementing once added homescreen
    }

    public void onSaveDrawingClick(View view) throws InterruptedException {
        Marker temp = null;
        latLngBounds = toBounds(latLng, maxDistanceFromCenter);
        llSaveClearContainer.setVisibility(View.GONE);
        if (marker != null) {
            temp = marker;
            llMarkerDroppedContainer.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.FadeIn).playOn(llMarkerDroppedContainer);
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(temp.getPosition()));
        } else {
            llMapActionContainer.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.FadeIn).playOn(llMapActionContainer);
            mMap.clear();
        }
        marker = temp;
        KnownArea knownArea = new KnownArea(latLngBounds.northeast.latitude, latLngBounds.northeast.longitude,
                latLngBounds.southwest.latitude, latLngBounds.southwest.longitude);
        mDatabase.child(mUser.getId()).push().setValue(knownArea);
    }

    public void onClearDrawingClick(View view) {
        Marker temp = null;
        llSaveClearContainer.setVisibility(View.GONE);
        if (marker != null) {
            temp = marker;
            llMarkerDroppedContainer.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.FadeIn).playOn(llMarkerDroppedContainer);
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(temp.getPosition()));
        } else {
            llMapActionContainer.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.FadeIn).playOn(llMapActionContainer);
            mMap.clear();
        }
        marker = temp;
    }

    public void onMarkerAddKnownAreaClick(View view) {
        llMarkerDroppedContainer.setVisibility(View.GONE);
        if (latLngs != null) {
            latLngs.clear();
        }
        drawingPanel.setVisibility(View.VISIBLE);
        drawingPanel.clear();
        polygonOptions = new PolygonOptions();
        llSaveClearContainer.setVisibility(View.VISIBLE);
        llSaveClearContainer.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.FadeIn).playOn(llSaveClearContainer);
    }

    public void onFindDirectionsClick(View view) {
        System.out.println("Clicking Find Directions");
        llMarkerDroppedContainer.setVisibility(View.GONE);
        llDirectionsFoundContainer.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.FadeIn).playOn(llDirectionsFoundContainer);
        sendRequest();
        Query query = mDatabase.orderByChild(mUser.getId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot areas : dataSnapshot.getChildren()){
                        for (DataSnapshot ds : areas.getChildren()){
                            nLat = (double) ds.child("nLat").getValue();
                            nLon = (double) ds.child("nLon").getValue();
                            sLat = (double) ds.child("sLat").getValue();
                            sLon = (double) ds.child("sLon").getValue();
                            System.out.println(nLat+" "+nLon+" "+sLat+" "+sLon);
                            LatLngBounds x = new LatLngBounds(new LatLng(sLat, sLon), new LatLng(nLat, nLon));
                            userBounds.add(x);
                        }
                    }
                    System.out.println("BOUNDS ARE: "+userBounds.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Toast.makeText()
            }
        });
    }

    public void onGoClick(View view) {
        mMap.setPadding(0, 250, 0, 105);
        llDirectionsFoundContainer.setVisibility(View.GONE);
        llDirectionsStartedContainer.setVisibility(View.VISIBLE);
        llNavDirContainer.setVisibility(View.VISIBLE);
        llTopPanelContainer.setVisibility(View.GONE);
        YoYo.with(Techniques.FadeIn).playOn(llDirectionsStartedContainer);
        YoYo.with(Techniques.FadeIn).playOn(llNavDirContainer);
        txtDirections.setText(text);
        //is destination name stored somewhere?
        t1.speak("Starting Directions", TextToSpeech.QUEUE_FLUSH, null);

        getLocation();
        LatLng currentLocation = new LatLng(lat, lon);
        int i = 0;
        while (inKnownArea == false && i < userBounds.size()){
            LatLngBounds b = userBounds.get(i);
            if (b.contains(currentLocation)){
                inKnownArea = true;
            }
            else{
                inKnownArea=false;
            }
            i++;
        }
        if (inKnownArea){
            t1.speak("Muting directions until out of known area.", TextToSpeech.QUEUE_ADD, null);
        }
        else if (!inKnownArea){
            t1.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng currentLocation = new LatLng(latitude, longitude);
                for (int i=0; i<userBounds.size(); i++){
                    LatLngBounds b = userBounds.get(i);
                    if (b.contains(currentLocation)){
                        inKnownArea = true;
                    }
                    else{
                        inKnownArea = false;
                    }
                }
                if(!(diri == dir.size()))
                {  double dirlat = Math.round(dir.get(diri).latitude * 1000.0)/1000.0;
                    double dirlng = Math.round(dir.get(diri).longitude * 1000.0)/1000.0;
                    double dirlat1;
                    double dirlng1;
                    Boolean dirlatcheck = false;
                    Boolean dirlngcheck = false;

                    dirlat1 = dirlat + .001;
                    dirlat = dirlat - .001;
                    dirlatcheck = (currentLocation.latitude >= dirlat) && (currentLocation.latitude <= dirlat1);


                    dirlng1 = dirlng + .001;
                    dirlng = dirlng - .001;
                    dirlngcheck = (currentLocation.longitude >= dirlng) && (currentLocation.longitude <= dirlng1);

                    if (dirlatcheck && dirlngcheck) {

                        text = ldir.get(ldiri);
                        text = text.replace("</b>", "");
                        text = text.replace("<b>", "");
                        text = text.replace("<div style=", "");
                        text = text.replace("font-size:0.9em", "");
                        text = text.replace(">", "");
                        text = text.replace("</div", "");
                        text = text.replace("</div]]", "");
                        text = text.replace("[[", "");
                        text = text.replace("&nbsp", "");
                        text = text.replace("]", "");
                        text = text.replace("[", "");
                        text = text.replace("Destination w", "\n" + "\n" + "Destination w");
                        text = text.replace(",", "\n" + "\n");
                        text = text.replace("&nbsp;", " ");

                        txtDirections.setText(text);

                        if (!inKnownArea) {
                            t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                        }

                        diri = diri + 1;
                        ldiri = ldiri + 1;
                    }
                }
                else
                {
                    t1.speak("Arrived at Destination", TextToSpeech.QUEUE_ADD, null);;
                    text="";
                    mMap.setPadding(0, 0, 0, 105);
                    llTopPanelContainer.setVisibility(View.VISIBLE);
                    llMapActionContainer.setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.FadeIn).playOn(llTopPanelContainer);
                    YoYo.with(Techniques.FadeIn).playOn(llMapActionContainer);
                    llNavDirContainer.setVisibility(View.GONE);
                    llDirectionsStartedContainer.setVisibility(View.GONE);
                    placeAutocompleteFragment.setText("");
                    mMap.clear();
                }
                moveToCurrentLocation(currentLocation);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });

    }

    public void onClearDirectionsClick(View view){
        placeAutocompleteFragment.setText("");
        llDirectionsFoundContainer.setVisibility(View.GONE);
        llMapActionContainer.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.FadeIn).playOn(llMapActionContainer);
        mMap.clear();
        getLocation();
        LatLng current = new LatLng(lat, lon);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 18));
    }

    public void onStopDirectionsClick(View view){
        mMap.setPadding(0, 0, 0, 105);
        llTopPanelContainer.setVisibility(View.VISIBLE);
        llMapActionContainer.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.FadeIn).playOn(llTopPanelContainer);
        YoYo.with(Techniques.FadeIn).playOn(llMapActionContainer);
        llNavDirContainer.setVisibility(View.GONE);
        llDirectionsStartedContainer.setVisibility(View.GONE);
        placeAutocompleteFragment.setText("");
        mMap.clear();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else {

            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (location != null) {
                lat = location.getLatitude();
                lon = location.getLongitude();
                latitude = String.valueOf(lat);
                longitude = String.valueOf(lon);

            }
            if (location1 != null) {
                lat = location1.getLatitude();
                lon = location1.getLongitude();
                latitude = String.valueOf(lat);
                longitude = String.valueOf(lon);


            } else if (location2 != null) {
                lat = location2.getLatitude();
                lon = location2.getLongitude();
                latitude = String.valueOf(lat);
                longitude = String.valueOf(lon);


            } else {

                Toast.makeText(this, "Unble to Trace your location", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private LatLng getPolygonCenterPoint(ArrayList<LatLng> polygonPointsList) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < polygonPointsList.size(); i++) {
            builder.include(polygonPointsList.get(i));
        }
        bounds = builder.build();
        latLng = bounds.getCenter();

        return latLng;
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        Location selected_location = new Location("locationA");
        selected_location.setLatitude(lat1);
        selected_location.setLongitude(lon1);
        Location near_locations = new Location("locationA");
        near_locations.setLatitude(lat2);
        near_locations.setLongitude(lon2);

        double distance = selected_location.distanceTo(near_locations);
        return distance;
    }

    private void sendRequest() {
        String destination = getAddress(marker.getPosition().latitude, marker.getPosition().longitude);
        System.out.println("DESTINATION IS: " + destination);

        String origin = getAddress(lat, lon);
        System.out.println("ORIGIN IS: " + origin);

        try {
            new DirectionFinder(this, origin, destination).execute();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        String add = null;
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            add = obj.getAddressLine(0);
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();

            Log.v("IGA", "Address" + add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();
            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return add;
    }

    public void onDirectionStringSuccess(String res) {
        System.out.println("Res is: "+res);
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please Wait,",
                "Finding directions!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        //System.out.println("Routes is: "+routes.toString());

        progressDialog.dismiss();
        polylinePaths = new ArrayList<Polyline>();
        originMarkers = new ArrayList<Marker>();
        destinationMarkers = new ArrayList<Marker>();

        for (Route route : routes) {
            for(int i = 0; i < route.directions.size(); i++)
            {
                ldir.add(route.directions.get(i).getDirection().toString());
                dir.add(route.directions.get(i).point);
            }
            System.out.println("test");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            System.out.println("POLYLINE IS: "+polylineOptions);

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }

        text = ldir.get(ldiri);
        text = text.replace("</b>", "");
        text = text.replace("<b>", "");
        text = text.replace("<div style=", "");
        text = text.replace("font-size:0.9em", "");
        text = text.replace(">", "");
        text = text.replace("</div", "");
        text = text.replace("</div]]", "");
        text = text.replace("[[", "");
        text = text.replace("]", "");
        text = text.replace("Destination w", "\n"+"\n"+"Destination w");
        text = text.replace(",", "\n"+"\n");
        text = text.replace("&nbsp;", " ");

        diri = diri + 1;
        ldiri = ldiri + 1;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 200) {
            if (resultCode == RESULT_OK) {
                try {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    String name = place.getName().toString();

                    latLngSource = place.getLatLng();
                    if (sourceMarker != null) {
                        sourceMarker.remove();
                    }

                    CameraUpdate updateSource = CameraUpdateFactory.newLatLngZoom(latLngSource, 15);
                    mMap.moveCamera(updateSource);


                    MarkerOptions optionsSource = new MarkerOptions();
                    optionsSource.title("Current Location");
                    optionsSource.position(latLngSource);
                    sourceMarker = mMap.addMarker(optionsSource);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        } else if (requestCode == 400) {
            if (resultCode == RESULT_OK) {

                try {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    latLngDestination = place.getLatLng();
                    String name = place.getName().toString();

                    //txtGo.setText(name);
                    if (destinationMarker != null) {
                        destinationMarker.remove();
                    }
                    CameraUpdate updateSource = CameraUpdateFactory.newLatLngZoom(latLngDestination, 15);
                    mMap.moveCamera(updateSource);

                    MarkerOptions optionsSource = new MarkerOptions();
                    optionsSource.title("Destination Location");
                    optionsSource.position(latLngDestination);
                    destinationMarker = mMap.addMarker(optionsSource);


                    // Direction
                    StringBuilder sb;

                    Object[] dataTransfer = new Object[4];

                    sb = new StringBuilder();
                    sb.append("https://maps.googleapis.com/maps/api/directions/json?");
                    sb.append("origin=" + latLngSource.latitude + "," + latLngSource.longitude);
                    sb.append("&destination=" + latLngDestination.latitude + "," + latLngDestination.longitude);
                    sb.append("&alternatives=true");


                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

        }

    }

    private void moveToCurrentLocation(LatLng currentLocation)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
    }

    public LatLngBounds toBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
    }

    @Override
    public void onStart() {
        super.onStart();

        // [START on_start_sign_in]
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account == null)
        {
            startActivity(new Intent( MapsActivity.this, MainActivity.class));
        }
        t1.stop();
        // [END on_start_sign_in]
    }

}
