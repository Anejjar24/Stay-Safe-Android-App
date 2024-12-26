package ma.ensaj.staysafe10.ui.location;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDateTime;

import ma.ensaj.staysafe10.R;
import ma.ensaj.staysafe10.databinding.ActivityMapsBinding;
import ma.ensaj.staysafe10.model.Location;
import ma.ensaj.staysafe10.ui.auth.user.UserViewModel;
import ma.ensaj.staysafe10.ui.location.viewmodel.LocationViewModel;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "MapsActivity";


    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private LocationViewModel locationViewModel;
    private UserViewModel userViewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModels
        locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Add location button
        setupAddLocationButton();
    }

    private void setupAddLocationButton() {
        FloatingActionButton fabAddLocation = new FloatingActionButton(this);
        fabAddLocation.setImageResource(android.R.drawable.ic_input_add);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.setMargins(0, 0, 32, 32);

        ((FrameLayout) binding.getRoot()).addView(fabAddLocation, params);

        fabAddLocation.setOnClickListener(v -> getCurrentLocationAndSave());
    }


    private void getCurrentLocationAndSave() {
        if (!isLocationEnabled()) {
            Toast.makeText(this, "Veuillez activer la localisation dans les paramètres", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }

        if (checkLocationPermission()) {
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10000);

            fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null && locationResult.getLastLocation() != null) {
                        android.location.Location location = locationResult.getLastLocation();
                        userViewModel.getCurrentUser().observe(MapsActivity.this, user -> {
                            if (user != null) {
                                saveLocation(location, user.getId());
                            }
                        });
                        fusedLocationClient.removeLocationUpdates(this);
                    }
                }
            }, Looper.getMainLooper());
        }
    }
    private void saveLocation(android.location.Location androidLocation, Long userId) {
        // Créer votre location personnalisée à partir de la location Android
        ma.ensaj.staysafe10.model.Location locationData = new ma.ensaj.staysafe10.model.Location();
        locationData.setLatitude(androidLocation.getLatitude());
        locationData.setLongitude(androidLocation.getLongitude());
        locationData.setTimestamp(LocalDateTime.now().toString());
        locationData.setUserId(userId);
        locationData.setIsEmergency(false);
        locationData.setStatus("ACTIVE");
        Log.d(TAG, " user ID: " + locationData);

        locationViewModel.createLocation(locationData).observe(this, created -> {
            if (created != null) {
                LatLng position = new LatLng(created.getLatitude(), created.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title("Ma position")
                );
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f));
                Toast.makeText(this, "Position enregistrée avec succès", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Erreur lors de l'enregistrement de la position", Toast.LENGTH_SHORT).show();
            }
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocationAndSave();
            } else {
                Toast.makeText(this, "Permission de localisation requise",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);

            // Get initial location
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                        }
                    });
        }
    }
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}

/*
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private LocationViewModel locationViewModel;
    private UserViewModel userViewModel;
    private boolean userLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialiser UserViewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Créer LocationViewModel
        locationViewModel = new LocationViewModel(userViewModel);

        // Observer l'utilisateur courant
        userViewModel.getCurrentUser().observe(this, user -> {
            userLoaded = (user != null);
        });

        // Observer les messages
        locationViewModel.getMessage().observe(this, message -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(latLng -> {
            if (userLoaded) {
                // Ajouter un marqueur
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Position sélectionnée"));

                // Sauvegarder la position
                locationViewModel.addCurrentUserLocation(
                        latLng.latitude,
                        latLng.longitude,
                        false
                );
            } else {
                Toast.makeText(this, "Veuillez attendre le chargement de l'utilisateur", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
*/