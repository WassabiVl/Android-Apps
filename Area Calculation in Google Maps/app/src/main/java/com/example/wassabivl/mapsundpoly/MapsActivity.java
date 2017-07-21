package com.example.wassabivl.mapsundpoly;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    /**
     * Define all Variables used in this activity
     */
    private GoogleMap mMap; //initiate Google maps
    SharedPreferences sharedPreferences; //Save the data to the shared preference when the map fragment closes.
    int locationCount = 0; // used to add the markers in the order they are clicked
    public ArrayList<LatLng> pointers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        pointers = new ArrayList<>(); //initializing the array to used for collecting pointers
        //to create the lines
        final PolylineOptions polylineOptions = new PolylineOptions();
        //setting the colour
        polylineOptions.color(Color.RED);
        // set the weight and visibility
        polylineOptions.width(2);
        polylineOptions.visible(true);
        //call upon area calculations when the button is pressed
        final Button button = (Button) findViewById(R.id.button);
        final String currentText = button.getText().toString();
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //add the points to the list
                polylineOptions.addAll(pointers);
                //draw the lines
                mMap.addPolyline(polylineOptions);
                //create the polygon
                Polygon polygon1 = mMap.addPolygon(new PolygonOptions().addAll(pointers));
                polygon1.setTag("alpha");
                polygon1.setFillColor(0xf00ff00);
                //change the text of the button
                button.setText(currentText.equals("Start Polygon") ? "End Polygon" : "Start Polygon");
                new Calculation().execute();
            }});
        }
    /**
     * Manipulates the map once available.
     * upon loading, the maps loads into Weimar, with the set corrdinates
     * and with a 13 level degree zoom
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        // Add a marker in Sydney and move the camera
        LatLng Wiemar = new LatLng(50.979492, 11.323544);
        float zoomLevel = 13;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Wiemar, zoomLevel));
    }
    @Override
    public void onMapLongClick(LatLng point) {
        //increase the number of locations.
        locationCount++;
        pointers.add(point);
        EditText editText = (EditText) findViewById(R.id.textView2);
        String text = editText.getText().toString();
        //confirm the marker registered
        Toast.makeText(MapsActivity.this,
                text + " : \n" + point.latitude + " : " + point.longitude, Toast.LENGTH_SHORT).show();
        MarkerOptions markerOptions = new MarkerOptions().position(point).title(text + point.toString());
        mMap.addMarker(markerOptions);
       /**
        * http://www.androidtrainee.com/adding-multiple-marker-locations-in-google-maps-android-api-v2-and-save-it-in-shared-preferences/
        */
        // Drawing marker on the map
        drawMarker(point);
        // Opening the sharedPreferences object
        sharedPreferences = getSharedPreferences("location", 0);
        // Getting number of locations already stored
        locationCount = sharedPreferences.getInt("locationCount", 0);
        // Getting stored zoom level if exists else return 0
        String zoom = sharedPreferences.getString("zoom", "0");
        try{
            // If locations are already saved
            if (locationCount != 0) {
                String lat = "";
                String lng = "";
                // Iterating through all the locations stored
                for (int i = 0; i < locationCount; i++) {
                    // Getting the latitude of the i-th location
                    lat = sharedPreferences.getString("lat" + i, "0");
                    // Getting the longitude of the i-th location
                    lng = sharedPreferences.getString("lng" + i, "0");
                }
                // Moving CameraPosition to last clicked position
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng))));
                // Setting the zoom level in the map on last position is clicked
                mMap.animateCamera(CameraUpdateFactory.zoomTo(Float.parseFloat(zoom)));
            }
            // Opening the editor object to write data to sharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("lat" + Integer.toString((locationCount - 1)), Double.toString(point.latitude));
            // Storing the longitude for the i-th location
            editor.putString("lng" + Integer.toString((locationCount - 1)), Double.toString(point.longitude));
            // Storing the count of locations or marker count
            editor.putInt("locationCount", locationCount);
            //Storing the zoom level to the shared preferences */
            editor.putString("zoom", Float.toString(mMap.getCameraPosition().zoom));
            //Saving the values stored in the shared preferences */
            editor.apply();}
        catch (NoSuchElementException e){
            Toast.makeText(MapsActivity.this,
                    e + " not valid", Toast.LENGTH_LONG).show();
        }
    }
    private class Calculation extends AsyncTask<Wrapper, Void, Wrapper> {
        Wrapper w = new Wrapper();
        @Override
        protected Wrapper doInBackground(Wrapper... params) {// http://googlemaps.github.io/android-maps-utils/
            // to calculate the area
            int z = pointers.size(); //so the loops knows when to stop.
            Double area = SphericalUtil.computeArea(pointers); //google library to compute spherical landmass
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (int i = 0; i < z; i++) {
                builder.include(pointers.get(i));
            }
            LatLngBounds bounds = builder.build();
            w.Centers = bounds.getCenter();
            w.AreaF = area;
            return  w; //enable the return of multiple values
        }
        @Override
        protected void onPostExecute(Wrapper result) {
            Toast.makeText(MapsActivity.this, "Area is " + w.AreaF.toString()+ "M^2", Toast.LENGTH_SHORT).show();
            mMap.addMarker(new MarkerOptions().position(w.Centers).title("Centriod With Area = " + w.AreaF.toString()+ "M^2")) ;
        }
    }
    private void drawMarker(LatLng latLng) {
        // Creating an instance of MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions();
        // Setting latitude and longitude for the marker
        markerOptions.position(latLng);
        // Adding marker on the Google Map
        mMap.addMarker(markerOptions);
    }
    class Wrapper {
        LatLng Centers;
        Double AreaF;
    }
}
