package ma.ensaj.staysafe10.ui.location;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.LocalDateTime;

import ma.ensaj.staysafe10.R;
import ma.ensaj.staysafe10.databinding.ActivityMaps2Binding;

import ma.ensaj.staysafe10.ui.auth.user.UserViewModel;
import ma.ensaj.staysafe10.ui.location.viewmodel.LocationViewModel;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "TrackActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final float DEFAULT_ZOOM = 15f;
    private static final long UPDATE_INTERVAL = 30000; // 30 seconds
    private static final long FASTEST_INTERVAL = 15000; // 15 seconds
    private GoogleMap mMap;
    private ActivityMaps2Binding binding;



    private LocationViewModel locationViewModel;
    private UserViewModel userViewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location lastSavedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMaps2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeComponents();
        setupMap();
    }
    private void setupMap() {
        ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map))
                .getMapAsync(this);
    }
    private void initializeComponents() {
        locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupLocationCallback();
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                Location currentLocation = locationResult.getLastLocation();
                if (shouldUpdateLocation(currentLocation)) {
                    userViewModel.getCurrentUser().observe(MapsActivity2.this, user -> {
                        if (user != null) {
                            saveLocation(currentLocation, user.getId());
                            lastSavedLocation = currentLocation;
                        }
                    });
                }
            }
        };
    }

    private boolean shouldUpdateLocation(Location currentLocation) {
        if (lastSavedLocation == null) return true;

        float distance = currentLocation.distanceTo(lastSavedLocation);
        return distance > 10; // Only update if moved more than 10 meters
    }

    private void startLocationUpdates() {
        if (!checkLocationServices()) return;
        if (!checkLocationPermission()) return;

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void saveLocation(Location location, Long userId) {
        ma.ensaj.staysafe10.model.Location locationData = createLocationData(location, userId);

        locationViewModel.createLocation(locationData).observe(this, created -> {
            if (created != null) {
                updateMapWithNewLocation(created);
            } else {
                Log.e(TAG, "Failed to save location");
            }
        });
    }

    private ma.ensaj.staysafe10.model.Location createLocationData(Location location, Long userId) {
        ma.ensaj.staysafe10.model.Location locationData = new ma.ensaj.staysafe10.model.Location();
        locationData.setLatitude(location.getLatitude());
        locationData.setLongitude(location.getLongitude());
        locationData.setTimestamp(LocalDateTime.now().toString());
        locationData.setUserId(userId);
        locationData.setIsEmergency(false);
        locationData.setStatus("ACTIVE");
        return locationData;
    }
    private void updateMapWithNewLocation(ma.ensaj.staysafe10.model.Location location) {
        LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions()
                .position(position)
                .title("Ma position")
        );
        mMap.animateCamera(CameraUpdateFactory.newLatLng(position));
    }






    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
            startLocationUpdates();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private boolean checkLocationServices() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isEnabled) {
            Toast.makeText(this, "Veuillez activer la localisation dans les paramètres", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
        return isEnabled;
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

}