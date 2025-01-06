package uk.ac.aston.cs3mdd.whichdayapp;

import androidx.fragment.app.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class CustomMapFragment extends SupportMapFragment  {
//
//  private GoogleMap googleMap;
//
//  @Override
//  public void onCreate(@Nullable Bundle savedInstanceState) {
//    super.onCreate(savedInstanceState);
//    getMapAsync(this); // Register the callback to initialize the map
//  }
//
//  @Override
//  public void onMapReady(@NonNull GoogleMap map) {
//    googleMap = map;
//
//    // Example: Add a marker at a default location
//    LatLng defaultLocation = new LatLng(51.5074, -0.1278); // London
//    googleMap.addMarker(new MarkerOptions().position(defaultLocation).title("Default Location"));
//    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
//
//    // Example: Configure UI settings
//    googleMap.getUiSettings().setZoomControlsEnabled(true);
//    googleMap.getUiSettings().setCompassEnabled(true);
//  }
//
//  // Optional: Add custom methods to interact with the map
//  public void addMarker(LatLng location, String title) {
//    if (googleMap != null) {
//      googleMap.addMarker(new MarkerOptions().position(location).title(title));
//    }
//  }
}

