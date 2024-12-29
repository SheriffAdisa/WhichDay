package uk.ac.aston.cs3mdd.whichdayapp;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executors;

import uk.ac.aston.cs3mdd.whichdayapp.database.AppDatabase;
import uk.ac.aston.cs3mdd.whichdayapp.database.Bookmark;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

  private GoogleMap googleMap;
  private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);

    // Setup Toolbar
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    // Initialize the map
    FragmentManager fragmentManager = getSupportFragmentManager();
    SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.mapFragmentContainer);

    if (mapFragment == null) {
      mapFragment = SupportMapFragment.newInstance();
      fragmentManager.beginTransaction()
              .replace(R.id.mapFragmentContainer, mapFragment)
              .commit();
    }

    mapFragment.getMapAsync(this);

    // Request location permissions if not already granted
    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }
  }

  @Override
  public void onMapReady(@NonNull GoogleMap map) {
    this.googleMap = map;

    // Configure map UI settings
    googleMap.getUiSettings().setZoomControlsEnabled(true);
    googleMap.getUiSettings().setMapToolbarEnabled(true);

    // Enable location if permission is granted
    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
      googleMap.setMyLocationEnabled(true);
    } else {
      ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    // Set marker click listener
    googleMap.setOnMarkerClickListener(marker -> {
      String cityName = marker.getTitle();
      LatLng position = marker.getPosition();

      // Fetch nearby cities when a marker is clicked
      fetchNearbyCities(position.latitude, position.longitude);
      Toast.makeText(this, "Fetching nearby cities for: " + cityName, Toast.LENGTH_SHORT).show();
      return true;
    });

    // Load bookmarks and add markers to the map
    loadAndDisplayBookmarks();
  }

  private void loadAndDisplayBookmarks() {
    AppDatabase db = AppDatabase.getInstance(this);

    Executors.newSingleThreadExecutor().execute(() -> {
      List<Bookmark> bookmarks = db.bookmarkDao().getAllBookmarks();

      runOnUiThread(() -> {
        if (bookmarks.isEmpty()) {
          // Set a default camera position
          LatLng defaultLocation = new LatLng(51.5074, -0.1278); // Example: London
          googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
        } else {
          for (Bookmark bookmark : bookmarks) {
            LatLng location = new LatLng(bookmark.getLatitude(), bookmark.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(bookmark.getCityName()));
          }

          // Center the map on the first bookmark
          LatLng firstLocation = new LatLng(
                  bookmarks.get(0).getLatitude(),
                  bookmarks.get(0).getLongitude());
          googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 10));
        }
      });
    });
  }

  private void fetchNearbyCities(double latitude, double longitude) {
    String apiKey = "AIzaSyCNvbZUQmHLWz_4mVYv7wX0YQPWGRl_XuM"; // Replace with your actual API key
    String location = latitude + "," + longitude;
    int radius = 50000; // Radius in meters (50km)

    String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
            + location + "&radius=" + radius + "&type=locality&key=" + apiKey;

    Executors.newSingleThreadExecutor().execute(() -> {
      try {
        URL requestUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          response.append(line);
        }
        reader.close();

        // Parse the JSON response
        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray results = jsonResponse.getJSONArray("results");

        runOnUiThread(() -> {
          googleMap.clear(); // Clear existing markers

          try {
            for (int i = 0; i < results.length(); i++) {
              JSONObject place = results.getJSONObject(i);
              JSONObject locationObj = place.getJSONObject("geometry").getJSONObject("location");
              double lat = locationObj.getDouble("lat");
              double lng = locationObj.getDouble("lng");
              String name = place.getString("name");

              LatLng position = new LatLng(lat, lng);
              googleMap.addMarker(new MarkerOptions()
                      .position(position)
                      .title(name));
            }

            // Center the map on the selected location
            LatLng selectedLocation = new LatLng(latitude, longitude);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 10));
          } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to parse places data", Toast.LENGTH_SHORT).show();
          }
        });
      } catch (Exception e) {
        e.printStackTrace();
        runOnUiThread(() -> Toast.makeText(this, "Failed to fetch nearby cities", Toast.LENGTH_SHORT).show());
      }
    });
  }
}
