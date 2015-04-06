package com.intersog.androidcourse.rimma.googlemaphw19;

import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private int locationId;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Location mPreviousLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        buildGoogleApiClient();
        createLocationRequest();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60 * 1000);
        mLocationRequest.setFastestInterval(30 * 1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        Log.i(TAG, "Location updates started.");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        Log.i(TAG, "Location updates stopped.");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
        }
    }


    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString() + locationId);
        mLastLocation = location;
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng currentLatLng = new LatLng(currentLatitude, currentLongitude);
        if (mPreviousLocation != null) {
            double previousLatitude = mPreviousLocation.getLatitude();
            double previousLongitude = mPreviousLocation.getLongitude();
            if (previousLatitude != currentLatitude || previousLongitude != currentLongitude) {
                addLocationMarker(currentLatLng);
                LatLng previousLatLng = new LatLng(previousLatitude, previousLongitude);
                mMap.addPolyline(new PolylineOptions().add(previousLatLng).add(currentLatLng).width(5).color(Color.RED));
                locationId++;
                mPreviousLocation = mLastLocation;
            }
        } else {
            addLocationMarker(currentLatLng);
            locationId++;
            mPreviousLocation = mLastLocation;
        }
    }

    private void addLocationMarker(LatLng latLng) {
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("#" + locationId);
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        if (mLastLocation == null) {
            Log.i(TAG, "mLastLocation == null");
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                handleNewLocation(mLastLocation);
            }
        }
        startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location changed");
        handleNewLocation(location);
    }
}
