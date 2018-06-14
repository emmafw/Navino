package com.example.simeonm.da;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.app.AlertDialog;

import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.androidanimations.library.Techniques;

import com.example.simeonm.da.view.DrawingPanel;
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
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.DirectionString;
import Modules.Route;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private double lat, lon;
    private String latitude, longitude;
    private LocationManager locationManager;
    private PlaceAutocompleteFragment placeAutocompleteFragment;
    private LinearLayout llMapActionContainer;
    private LinearLayout llSaveClearContainer;
    private LinearLayout llMarkerDroppedContainer;
    private FrameLayout flMapContainer;
    Marker marker, sourceMarker, destinationMarker;
    private ArrayList<LatLng> latLngs;
    private PolygonOptions polygonOptions;
    private LatLngBounds latLngBounds, bounds;
    private DrawingPanel drawingPanel;
    double maxDistanceFromCenter;
    private Projection projection;
    private LatLng latLng, latLngSource, latLngDestination;
    private DirectionFinderListener listener;
    private TextView txtGo, txtNav;
    LinearLayout navi;
    String text;
    TextToSpeech t1;
    TextView textDir;
    private ArrayList<Marker> originMarkers, destinationMarkers;
    private ArrayList<Polyline> polylinePaths;
    private ProgressDialog progressDialog;
    List<String> ldir = new ArrayList<String>();
    List<LatLng> dir = new ArrayList<LatLng>();
    int ldiri = 0;
    int diri = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getLocation();

        }
        textDir= (TextView)findViewById(R.id.textDir);
        textDir.setText("");
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // [END build_client]
        TextView logout = (TextView)findViewById(R.id.textSignout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleSignInClient.signOut();
                startActivity(new Intent(MapsActivity.this, MainActivity.class));

            }
        });
        navi = (LinearLayout) findViewById(R.id.navi);
        navi.setVisibility(View.GONE);
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
                llMapActionContainer.setVisibility(View.INVISIBLE);
                llSaveClearContainer.setVisibility(View.INVISIBLE);
                txtNav.setVisibility(View.GONE);
                txtGo.setVisibility(View.VISIBLE);
                t1.stop();
                navi.setVisibility(View.GONE);
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(MapsActivity.this, "" + status.toString(), Toast.LENGTH_SHORT).show();
            }


        });
        llMapActionContainer = (LinearLayout) findViewById(R.id.llMapActionContainer);
        llSaveClearContainer = (LinearLayout) findViewById(R.id.llSaveOrClearContainer);
        flMapContainer = (FrameLayout) findViewById(R.id.flMapContainer);
        llMarkerDroppedContainer = (LinearLayout) findViewById(R.id.llMarkerDropedContainer);
        txtGo = (TextView) findViewById(R.id.textGO);
        txtNav = (TextView) findViewById(R.id.textNav);
        drawingPanel = new DrawingPanel(this);

        txtNav.setVisibility(View.GONE);
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
        txtGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Sending request...");
                sendRequest();
                txtGo.setVisibility(View.GONE);
                txtNav.setVisibility(View.VISIBLE);
            }
        });


        txtNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtNav.getText().equals("Navigate")) {
                    txtNav.setText("End Navigation");
                    textDir.setText(text);
                    navi.setVisibility(View.VISIBLE);
                    t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);



                } else {
                    t1.stop();
                    navi.setVisibility(View.INVISIBLE);
                    txtNav.setText("Navigate");
                }
            }
        });


    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Turn ON your GPS Connection")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
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

                    txtGo.setText(name);
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

    protected void buildAlertMessageNoGPS() {


    }


    private void sendRequest() {
        String destination = getAddress(marker.getPosition().latitude, marker.getPosition().longitude);
        System.out.println("DESTINATION IS: " + destination);

        String origin = getAddress(lat, lon);
        System.out.println("ORIGIN IS: " + origin);

        try {
            new DirectionFinder(this, origin, destination).execute();
            new DirectionString(this, origin, destination).execute();

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(0, 0, 0, 105);
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

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if (mMap != null) {
                    llMapActionContainer.setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.FadeIn).playOn(llMapActionContainer);
                }
            }
        });

        drawingPanel.setOnDragListener(new DrawingPanel.OnDragListener() {
            @Override
            public void onDragEvent(MotionEvent motionEvent) {

                //Track touch point
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

    public void onLocationChanged(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        ;
        LatLng myLatLng = new LatLng(latitude, longitude);
        double dirlat = Math.round(dir.get(diri).latitude * 10000.0);
        double dirlng = Math.round(dir.get(diri).longitude * 10000.0);
        double dirlat1;
        double dirlng1;
        Boolean dirlatcheck = false;
        Boolean dirlngcheck = false;
        if(dirlat < dir.get(diri).latitude)
        {
            dirlat1 = dirlat + .0001;
            dirlatcheck = (myLatLng.latitude >= dirlat) && (myLatLng.latitude <= dirlat1);
        }
        else
        {
            dirlat1 = dirlat - .0001;
            dirlatcheck = (myLatLng.latitude <= dirlat) && (myLatLng.latitude >= dirlat1);
        }

        if(dirlng < dir.get(diri).longitude)
        {
            dirlng1 = dirlng + .0001;
            dirlngcheck = (myLatLng.longitude >= dirlng) && (myLatLng.latitude <= dirlng1);
        }
        else
        {
            dirlng1 = dirlng - .0001;
            dirlngcheck = (myLatLng.longitude <= dirlng) && (myLatLng.latitude >= dirlng1);
        }


        if(dirlatcheck && dirlngcheck)
        {

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
            text = text.replace("Destination w", "\n"+"\n"+"Destination w");
            text = text.replace(",", "\n"+"\n");

            diri = diri + 1;
            ldiri = ldiri + 1;
        }


        moveToCurrentLocation(myLatLng);

    }

    private void moveToCurrentLocation(LatLng currentLocation)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.


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
    public void onDirectionStringSuccess(String res) {
        System.out.println("Res is: "+res);
    }
    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        System.out.println("Routes is: "+routes);

        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();


        for (Route route : routes) {
            for(int i = 0; i < route.directions.size(); i++)
            {
                ldir.add(route.directions.get(i).getDirection());
                dir.add(route.directions.get(i).point);
            }
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
    }

    public void onAddKnownAreaClick(View view) {

        if (latLngs != null)
            latLngs.clear();
        //mMap.clear();
        llMapActionContainer.setVisibility(View.GONE);

        drawingPanel.setVisibility(View.VISIBLE);
        drawingPanel.clear();
        polygonOptions = new PolygonOptions();
        llSaveClearContainer.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.FadeIn).playOn(llSaveClearContainer);
    }

    public void onSaveDrawingClick(View view){
        Marker temp = null;
        latLngBounds = toBounds(latLng, maxDistanceFromCenter);
        llSaveClearContainer.setVisibility(View.GONE);
        if (marker != null){
            temp = marker;
            llMarkerDroppedContainer.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.FadeIn).playOn(llMarkerDroppedContainer);
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(temp.getPosition()));
        }
        else{
            llMapActionContainer.setVisibility(View.VISIBLE);
            mMap.clear();
        }
        marker = temp;
        UUID knownAreaID = UUID.randomUUID();
        System.out.println("UUID IS: "+knownAreaID);
    }

    public void onClearDrawingClick(View view){
        Marker temp = null;
        llSaveClearContainer.setVisibility(View.GONE);
        if (marker != null){
            temp = marker;
            llMarkerDroppedContainer.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.FadeIn).playOn(llMarkerDroppedContainer);
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(temp.getPosition()));
        }
        else{
            llMapActionContainer.setVisibility(View.VISIBLE);
            mMap.clear();
        }
        marker = temp;
    }

    public LatLngBounds toBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
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

    private LatLng getPolygonCenterPoint(ArrayList<LatLng> polygonPointsList) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < polygonPointsList.size(); i++) {
            builder.include(polygonPointsList.get(i));
        }
        bounds = builder.build();
        latLng = bounds.getCenter();

        return latLng;
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
