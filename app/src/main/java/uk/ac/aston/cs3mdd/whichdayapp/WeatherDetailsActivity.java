package uk.ac.aston.cs3mdd.whichdayapp;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import java.util.ArrayList;

import uk.ac.aston.cs3mdd.whichdayapp.database.AppDatabase;
import uk.ac.aston.cs3mdd.whichdayapp.models.DaySummary;
import uk.ac.aston.cs3mdd.whichdayapp.models.FavoriteCity;

public class WeatherDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {


  private GoogleMap mMap; // Google Map instance
  private double cityLat; // Latitude of the city
  private double cityLon; // Longitude of the city
  private String cityName; // City name


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_weather_details);

    // Set up the Toolbar
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    // Enable the back button in the action bar
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle("Your Recommendation");
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

    Button buttonAddToFavorites = findViewById(R.id.buttonAddToFavorites);
    buttonAddToFavorites.setOnClickListener(v -> {
      String cityName = getIntent().getStringExtra("cityName");
      if (cityName != null && !cityName.isEmpty()) {
        AppDatabase db = AppDatabase.getInstance(this);
        new Thread(() -> {
          db.favoriteCityDao().insertCity(new FavoriteCity(cityName));
          runOnUiThread(() -> Toast.makeText(this, "City added to favorites", Toast.LENGTH_SHORT).show());
        }).start();
      }
    });


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
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {

    if (item.getItemId() == android.R.id.home) {
      // Navigate back to the previous activity
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }


  private void saveToFavorites() {
    // Logic to save the recommendation to the database
    // Example: Show a Toast as a placeholder
    Toast.makeText(this, "Saved to Favorites!", Toast.LENGTH_SHORT).show();
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_weather_details, menu);
    return true; // Return true to display the menu
  }



}
