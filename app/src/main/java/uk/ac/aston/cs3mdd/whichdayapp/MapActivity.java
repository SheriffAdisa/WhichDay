package uk.ac.aston.cs3mdd.whichdayapp;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uk.ac.aston.cs3mdd.whichdayapp.database.AppDatabase;
import uk.ac.aston.cs3mdd.whichdayapp.database.Bookmark;
import uk.ac.aston.cs3mdd.whichdayapp.models.DaySummary;
import uk.ac.aston.cs3mdd.whichdayapp.models.WeatherItem;
import uk.ac.aston.cs3mdd.whichdayapp.models.WeatherResponse;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

  private GoogleMap googleMap;
  private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

  private static final String BASE_URL = "https://api.openweathermap.org/";
  private static final String API_KEY = "796b2ffe49982b3d99a31e32d87ff3ef";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);

    // Setup Toolbar
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle("Map View");
    }

    // Handle back navigation
    toolbar.setNavigationOnClickListener(v -> onBackPressed());

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

      // Fetch weather data for the clicked city
      fetchWeatherDataForCity(cityName);
      return true; // Indicate that the click is handled
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

  private void fetchWeatherDataForCity(String cityName) {
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    WeatherApi weatherApi = retrofit.create(WeatherApi.class);
    Call<WeatherResponse> call = weatherApi.getWeatherByCityName(cityName, API_KEY);

    call.enqueue(new Callback<WeatherResponse>() {
      @Override
      public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
          WeatherResponse weatherResponse = response.body();

          // Process weather data
          Map<String, List<WeatherItem>> dailyForecasts = MainActivity.groupForecastsByDay(weatherResponse.getList());
          List<DaySummary> summaries = MainActivity.calculateDailySummaries(dailyForecasts);

          // Find the recommended day
          DaySummary bestDay = MainActivity.getBestDay(summaries);

          runOnUiThread(() -> {
            if (bestDay != null) {
              showPopup(cityName, bestDay.getDate(), bestDay.getDescription());
            } else {
              showPopup(cityName, "No recommendation available", "");
            }
          });
        } else {
          runOnUiThread(() -> showPopup(cityName, "No recommendation available", ""));
        }
      }

      @Override
      public void onFailure(Call<WeatherResponse> call, Throwable t) {
        runOnUiThread(() -> showPopup(cityName, "Error fetching data", t.getMessage()));
      }
    });
  }

  private void showPopup(String cityName, String recommendedDay, String description) {
    new AlertDialog.Builder(this)
            .setTitle(cityName)
            .setMessage("Recommended Day: " + recommendedDay + "\nDescription: " + description)
            .setPositiveButton("OK", null)
            .show();
  }
}
