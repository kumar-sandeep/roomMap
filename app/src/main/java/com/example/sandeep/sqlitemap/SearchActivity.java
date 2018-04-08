package com.example.sandeep.sqlitemap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.LocaleDisplayNames;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = SearchActivity.class.getSimpleName();
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    GoogleMap mMap;
    @BindView(R.id.search_address)
    EditText addressEditext;
    PlaceModel placeModel;
    Place place;
    AppDatabase db;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }


    @OnClick(R.id.search_address)
    public void searchAddress() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                this.place = place;
                if (place != null) {
                    Log.i(TAG, "Place Name: " + place.getName());
                    Log.i(TAG, "Place Address: " + place.getAddress());
                    Log.i(TAG, "Place Id: " + place.getId());
                    Log.i(TAG, "Place LatLng: " + place.getLatLng());
                    addressEditext.setText(place.getAddress());
                    if (place.getLatLng() != null)
                        addMarker(place);
                }

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
            }
        }
    }

    private void addMarker(Place place) {
        Log.d(TAG, "addMarker called with place " + place);
        if (mMap != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(place.getLatLng())
                    .title(place.getName().toString()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 12));
        } else {
            Log.e(TAG, "Adding Map Failed mMap is " + mMap);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(50, 210, 50, 150);
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
        if (place == null) {
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
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(location.getLatitude(), location.getLongitude()), 13);
                                mMap.moveCamera(cameraUpdate);
                            }
                        }
                    });
        }
    }

    @OnClick(R.id.save_location)
    public void saveLocation() {
        db = AppDatabase.getDatabase(this);
        if (place != null && place.getName() != null && place.getAddress() != null && place.getLatLng() != null) {
            placeModel = new PlaceModel(place.getId(), place.getName().toString()
                    , place.getAddress().toString(), place.getLatLng().latitude
                    , place.getLatLng().longitude);
            try {
                Application application = getApplication();
                long returnedValue = new PlaceRepository(application).insert(placeModel);
                if (returnedValue != -1) {
                    Toast.makeText(SearchActivity.this, "Location Saved", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SearchActivity.this, "Failed to Save Location", Toast.LENGTH_LONG).show();
                }
                Log.d(TAG, "Inside saveLocation db insert location " + returnedValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Unable to fetch Location", Toast.LENGTH_LONG).show();
        }

    }
}