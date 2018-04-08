package com.example.sandeep.sqlitemap;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.icu.text.AlphabeticIndex;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

import butterknife.BindView;

public class ViewActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = ViewActivity.class.getSimpleName();
    GoogleMap mMap;
    List<PlaceModel> placeModelList;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        mapFragment.getMapAsync(this);
        PlaceViewModel placeViewModel = ViewModelProviders.of(this).get(PlaceViewModel.class);
        final Observer<List<PlaceModel>> placeObserver =
                new Observer<List<PlaceModel>>() {
                    @Override
                    public void onChanged(@Nullable List<PlaceModel> placeModels) {
                        Log.d(TAG, " list in observable is " + placeModels);
                        placeModelList = placeModels;
                        prepareUI();
                    }
                };
        placeViewModel.getPlaceModelList().observe(this, placeObserver);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    private void addMarker(PlaceModel place) {
        Log.d(TAG, "addMarker called with place " + place);
        if (mMap != null) {
            Marker m = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(place.getLatitude(), place.getLongitude())).title(place.getName()));
        } else {
            Log.e(TAG, "Adding Map Failed mMap is " + mMap);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(50, 210, 50, 50);
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

    }

    public void prepareUI() {
        try {
            Log.d(TAG, " Inside onMapReady() list from db is " + placeModelList);
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            if (placeModelList != null && !placeModelList.isEmpty()) {
                for (PlaceModel placeModel : placeModelList) {
                    addMarker(placeModel);
                    boundsBuilder.include(new LatLng(placeModel.getLatitude(), placeModel.getLongitude()));
                }
                LatLngBounds latLngBounds = boundsBuilder.build();
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 150));
                Toast.makeText(this, "Click on Marker to get directions", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ViewActivity.this, "No Places Added", Toast.LENGTH_LONG).show();
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
