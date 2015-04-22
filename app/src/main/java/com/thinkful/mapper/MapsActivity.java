package com.thinkful.mapper;

import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;

public class MapsActivity extends FragmentActivity implements ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private final static String TAG = "MAPPER";
    private Location mLastLocation = null;
    private Location mCurrentLocation = null;
    public static final String PREFS_NAME = "MyLastLocation";
    public static final String PREFS_KEY_LAT = "LastLat";
    public static final String PREFS_KEY_LNG = "LastLng";
    private Marker mMarker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        Float lat = settings.getFloat(PREFS_KEY_LAT, -1);
        Float lng = settings.getFloat(PREFS_KEY_LNG, -1);
        if (lat != -1) {
            Log.i(TAG, "Last location found!!");
            mLastLocation = latLongToLocation(new LatLng(lat, lng));
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        setUpMapIfNeeded();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        //stop location updates
        LocationServices.FusedLocationApi.removeLocationUpdates(
            mGoogleApiClient, this);
    }

    @Override
    protected void onStop(){
        Log.i(TAG, "onStop - saving location: " + mCurrentLocation);
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(PREFS_KEY_LAT, (float) mCurrentLocation.getLatitude());
        editor.putFloat(PREFS_KEY_LNG, (float) mCurrentLocation.getLongitude());
        // Commit the edits!
        editor.commit();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            setUpMapIfNeeded();    // <-from previous tutorial
            startLocationUpdates();
        }
    }
    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            Log.i(TAG, "mMap is null");
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                Log.i(TAG, "Successfully obtained map");
                mMap.setMyLocationEnabled(true);
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        Log.i(TAG, "setUpMap");
        Log.i(TAG, "Last location: " + mLastLocation);
        //showThinkfulHq();
        //showLocation(latLongToLocation(new LatLng(37.31562812,-121.98616366)));
    }

    private Location latLongToLocation(LatLng latLng) {
        Location location = new Location("Test Location");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        location.setTime(new Date().getTime()); //Set time as current Date
        return location;
    }


    /**
     * Called when Google API is connected
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "onConnected");
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        Log.i(TAG, "Last location: " + mLastLocation);
        Log.i(TAG, "Current location: " + mCurrentLocation);
        showLocationAnimate(mCurrentLocation);
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    private Location getLocationFromLocationService() {
        Log.i(TAG, "getLocationFromLocationService");
        Criteria criteria = new Criteria();
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            Log.i(TAG, "getLocationFromLocationService" + location);
            return location;
        }
        return null;
    }

    private void showThinkfulHq() {
        Log.i(TAG, "In setUpMap, set type to normal");
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng thinkfulLatLng = new LatLng(40.72493, -73.996599);
        Handler handler = new Handler();
        if (mMarker != null) {
            Log.i("Removing old marker");
            mMarker.remove();
        }
        mMarker = mMap.addMarker(new MarkerOptions()
                        .position(thinkfulLatLng)
                        .title("Thinkful Headquarters")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.thinkful))
                        .snippet("On a mission to reinvent education")
        );
        CameraUpdate update = CameraUpdateFactory.newLatLng(thinkfulLatLng);
        mMap.moveCamera(update);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMap.animateCamera(CameraUpdateFactory.zoomTo(19),2000,null);
            }
        }, 2000);
        mMarker.showInfoWindow();
    }

    private void showLocationNoAnimate(Location location) {
        showLocation(mCurrentLocation, false);
    }

    private void showLocationAnimate(Location location) {
        showLocation(location, true);
    }

    private void showLocation(Location location, boolean animate) {
        Log.i(TAG, "showCurrentLocation");
        Log.i("MAPPER Where am I=", String.valueOf(location));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (location != null) {
            LatLng currentLatLng = new LatLng(location.getLatitude(),
                    location.getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(currentLatLng)
                    .title("My Current Location")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.android_small
                    ))
                    .snippet("I am at a cozy place"));
            marker.showInfoWindow();
            final CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 18);
            if (animate) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMap.animateCamera(update, 2000, null);
                    }
                }, 2000);
            } else {
                mMap.moveCamera(update);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged: " + location);
        mCurrentLocation = location;
        showLocationAnimate(location);
    }
}
