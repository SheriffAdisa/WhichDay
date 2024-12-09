package uk.ac.aston.cs3mdd.whichdayapp;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import uk.ac.aston.cs3mdd.whichdayapp.models.DaySummary;

public class WeatherDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {


  private GoogleMap mMap; // Google Map instance
  private double cityLat; // Latitude of the city
  private double cityLon; // Longitude of the city
  private String cityName; // City name


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_weather_details);

    // Enable the back button in the action bar
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle("Your Recommendation"); // Set title
    }

    // Get data passed from MainActivity
    cityLat = getIntent().getDoubleExtra("cityLat", 0.0);
    cityLon = getIntent().getDoubleExtra("cityLon", 0.0);
    cityName = getIntent().getStringExtra("cityName");


    // Initialize the Map Fragment
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.mapFragment);
    if (mapFragment != null) {
      mapFragment.getMapAsync(this);
    }



    // Other setup (e.g., ListView, Recommended Day, etc.)
    setupUI();
  }

  // Configure the Google Map when it’s ready
  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    // Add a marker for the city
    LatLng cityLocation = new LatLng(cityLat, cityLon);
    mMap.addMarker(new MarkerOptions().position(cityLocation).title("Weather in " + cityName));
    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityLocation, 10));
  }

  // Setup other UI elements (Recommended Day, ListView)
  private void setupUI() {
    TextView recommendedDayView = findViewById(R.id.recommendedDayView);
    ListView forecastListView = findViewById(R.id.forecastListView);

    // Display recommended day
    String recommendedDay = getIntent().getStringExtra("recommendedDay");
    recommendedDayView.setText("Recommended Day: " + recommendedDay);

    // Display 5-day forecast
    ArrayList<DaySummary> summaries = getIntent().getParcelableArrayListExtra("summaries");
    ArrayAdapter<DaySummary> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, summaries);
    forecastListView.setAdapter(adapter);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Check if the home button (up button) was clicked
    if (item.getItemId() == android.R.id.home) {
      finish(); // Close the current activity and return to the previous one
      return true;
    }
    return super.onOptionsItemSelected(item);
  }


}
