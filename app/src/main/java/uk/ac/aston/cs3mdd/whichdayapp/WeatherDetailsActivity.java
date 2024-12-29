package uk.ac.aston.cs3mdd.whichdayapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uk.ac.aston.cs3mdd.whichdayapp.database.AppDatabase;
import uk.ac.aston.cs3mdd.whichdayapp.database.Bookmark;
import uk.ac.aston.cs3mdd.whichdayapp.models.DaySummary;

public class WeatherDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
  private GoogleMap mMap; // Google Map instance
  private double cityLat; // Latitude of the city
  private double cityLon; // Longitude of the city
  private String cityName; // City name

  private boolean isBookmarked = false; // Track bookmark state
  private MenuItem bookmarkMenuItem; // Menu item for bookmark icon

  private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // For background operations

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_weather_details);

    // Set up the Toolbar
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    // Enable the back button in the toolbar
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle("Your Recommendation");
    }

    // Retrieve data passed from MainActivity
    cityLat = getIntent().getDoubleExtra("cityLat", 0.0);
    cityLon = getIntent().getDoubleExtra("cityLon", 0.0);
    cityName = getIntent().getStringExtra("cityName");

    // Initialize the Map Fragment
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.mapFragment);
    if (mapFragment != null) {
      mapFragment.getMapAsync(this);
    }

    // Request location permissions if not already granted
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    // Check if the city is already bookmarked in a background thread
    executorService.execute(() -> {
      isBookmarked = AppDatabase.getInstance(this).bookmarkDao().isBookmarked(cityName);
      runOnUiThread(this::updateBookmarkIcon); // Update the bookmark icon on the UI thread
    });

    // Update UI elements
    setupUI();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_weather_details, menu);
    bookmarkMenuItem = menu.findItem(R.id.menu_bookmark); // Reference the bookmark menu item
    updateBookmarkIcon(); // Set the initial state of the icon
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.menu_bookmark) {
      toggleBookmark(); // Add or remove the bookmark
      return true;
    } else if (item.getItemId() == android.R.id.home) {
      onBackPressed(); // Handle back button press
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void updateBookmarkIcon() {
    if (bookmarkMenuItem != null) {
      if (isBookmarked) {
        bookmarkMenuItem.setIcon(R.drawable.ic_addedtobookmarks); // Set "bookmarked" icon
      } else {
        bookmarkMenuItem.setIcon(R.drawable.ic_addtobookmarks); // Set "not bookmarked" icon
      }
    }
  }

  private void toggleBookmark() {
    executorService.execute(() -> {
      AppDatabase db = AppDatabase.getInstance(this);

      if (isBookmarked) {
        // Remove from bookmarks
        db.bookmarkDao().deleteByName(cityName);
        isBookmarked = false;
        runOnUiThread(() -> {
          updateBookmarkIcon();
          Toast.makeText(this, cityName + " removed from bookmarks", Toast.LENGTH_SHORT).show();
        });
      } else {
        // Add to bookmarks
        Bookmark bookmark = new Bookmark(cityName, cityLat, cityLon);
        db.bookmarkDao().insert(bookmark);

        Log.d("Bookmarks", cityName + " added to database");
        isBookmarked = true;
        runOnUiThread(() -> {
          updateBookmarkIcon();
          Toast.makeText(this, cityName + " added to bookmarks", Toast.LENGTH_SHORT).show();
        });
      }
    });
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    // Enable location if permission is granted
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
      mMap.setMyLocationEnabled(true);
    }

    // Add a marker for the city
    LatLng cityLocation = new LatLng(cityLat, cityLon);
    mMap.addMarker(new MarkerOptions().position(cityLocation).title("Weather in " + cityName));
    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityLocation, 10));
  }

  private void setupUI() {
    // Display the recommended day and 5-day forecast
    TextView recommendedDayView = findViewById(R.id.recommendedDayView);
    ListView forecastListView = findViewById(R.id.forecastListView);

    // Display recommended day
    String recommendedDay = getIntent().getStringExtra("recommendedDay");
    if (recommendedDay != null) {
      recommendedDayView.setText("Recommended Day: " + recommendedDay);
    } else {
      recommendedDayView.setText("No recommended day available.");
    }

    // Display 5-day forecast
    ArrayList<DaySummary> summaries = getIntent().getParcelableArrayListExtra("summaries");
    if (summaries != null && !summaries.isEmpty()) {
      ArrayAdapter<DaySummary> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, summaries);
      forecastListView.setAdapter(adapter);
    } else {
      forecastListView.setAdapter(null);
      Toast.makeText(this, "No forecast data available.", Toast.LENGTH_SHORT).show();
    }
  }
}
