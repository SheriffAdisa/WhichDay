package uk.ac.aston.cs3mdd.whichdayapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Outline;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uk.ac.aston.cs3mdd.whichdayapp.adapters.ForecastAdapter;
import uk.ac.aston.cs3mdd.whichdayapp.database.AppDatabase;
import uk.ac.aston.cs3mdd.whichdayapp.database.Bookmark;
import uk.ac.aston.cs3mdd.whichdayapp.models.DaySummary;

public class WeatherDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
  private GoogleMap mMap;
  private double cityLat;
  private double cityLon;
  private String cityName;
  private boolean isBookmarked = false;
  private MenuItem bookmarkMenuItem;
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_weather_details);

    setupToolbar();
    retrieveIntentData();
    setupMapFragment();
    requestLocationPermission();
    checkIfBookmarked();
    setupUI();
  }

  private void setupToolbar() {
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle("Your Recommendation");
    }
  }

  private void retrieveIntentData() {
    cityLat = getIntent().getDoubleExtra("cityLat", 0.0);
    cityLon = getIntent().getDoubleExtra("cityLon", 0.0);
    cityName = getIntent().getStringExtra("cityName");

    Log.d("IntentData", "City Lat: " + cityLat);
    Log.d("IntentData", "City Lon: " + cityLon);
    Log.d("IntentData", "City Name: " + cityName);
  }

  private void setupMapFragment() {
    SupportMapFragment mapFragment = SupportMapFragment.newInstance();
    getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.mapFragment, mapFragment)
            .commit();

    mapFragment.getMapAsync(this);
  }

  private void requestLocationPermission() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }
  }

  private void checkIfBookmarked() {
    executorService.execute(() -> {
      isBookmarked = AppDatabase.getInstance(this).bookmarkDao().isBookmarked(cityName);
      runOnUiThread(this::updateBookmarkIcon);
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_weather_details, menu);
    bookmarkMenuItem = menu.findItem(R.id.menu_bookmark);
    updateBookmarkIcon();
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.menu_bookmark) {
      toggleBookmark();
      return true;
    } else if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void updateBookmarkIcon() {
    if (bookmarkMenuItem != null) {
      bookmarkMenuItem.setIcon(isBookmarked
              ? R.drawable.ic_addedtobookmarks
              : R.drawable.ic_addtobookmarks);
    }
  }

  private void updateBookmarkLabel() {
    TextView bookmarkLabel = findViewById(R.id.bookmarkLabel);

    if (isBookmarked) {
      bookmarkLabel.setText("City Saved:");
    } else {
      bookmarkLabel.setText("Save City");
    }

    bookmarkLabel.setVisibility(View.VISIBLE);
  }


  private void toggleBookmark() {
    executorService.execute(() -> {
      AppDatabase db = AppDatabase.getInstance(this);

      if (isBookmarked) {
        db.bookmarkDao().deleteByName(cityName);
        isBookmarked = false;
      } else {
        Bookmark bookmark = new Bookmark(cityName, cityLat, cityLon);
        db.bookmarkDao().insert(bookmark);
        isBookmarked = true;
      }

      runOnUiThread(() -> {
        updateBookmarkIcon();
        updateBookmarkLabel();
        String message = isBookmarked
                ? cityName + " Added to bookmarks"
                : cityName + " Removed from bookmarks";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
      });
    });
  }


  @Override
  public void onMapReady(GoogleMap googleMap) {
    try {
      mMap = googleMap;

      LatLng cityLocation = new LatLng(cityLat, cityLon);
      mMap.addMarker(new MarkerOptions()
              .position(cityLocation)
              .title("Weather in " + cityName));
      mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityLocation, 10));

      if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
              == PackageManager.PERMISSION_GRANTED) {
        mMap.setMyLocationEnabled(true);
      }
    } catch (Exception e) {
      Log.e("MapError", "Error loading map: " + e.getMessage(), e);
      Toast.makeText(this, "Error loading map: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  private void setupUI() {
    // Recommended Day Section
    TextView recommendedDateView = findViewById(R.id.recommendedDate);
    ImageView recommendedWeatherIcon = findViewById(R.id.recommendedWeatherIcon);
    TextView recommendedTempView = findViewById(R.id.recommendedTemp);
    TextView recommendedDescriptionView = findViewById(R.id.recommendedDescription);

    String recommendedDay = getIntent().getStringExtra("recommendedDay");
    String recommendedDescription = getIntent().getStringExtra("recommendedDescription");
    double recommendedTemp = getIntent().getDoubleExtra("recommendedTemp", 0.0);


    Log.d("IntentData", "Recommended Day: " + recommendedDay);
    Log.d("IntentData", "Recommended Description: " + recommendedDescription);
    Log.d("IntentData", "Recommended Temp: " + recommendedTemp);


    if (recommendedDay != null && recommendedDescription != null) {
      recommendedDateView.setText(formatDate(recommendedDay));
      recommendedTempView.setText(String.format(Locale.getDefault(), "%.1f°C", recommendedTemp));
      recommendedDescriptionView.setText(recommendedDescription);
      recommendedWeatherIcon.setImageResource(getWeatherIcon(recommendedDescription));
    } else {
      recommendedDateView.setText("N/A");
      recommendedTempView.setText("N/A");
      recommendedDescriptionView.setText("N/A");
      recommendedWeatherIcon.setImageResource(R.drawable.ic_unknown);
    }

    // Forecast Section
    RecyclerView forecastRecyclerView = findViewById(R.id.forecastRecyclerView);
    ArrayList<DaySummary> summaries = getIntent().getParcelableArrayListExtra("summaries");
    if (summaries != null && !summaries.isEmpty()) {
      ForecastAdapter adapter = new ForecastAdapter(summaries);
      forecastRecyclerView.setLayoutManager(new LinearLayoutManager(this));
      forecastRecyclerView.setAdapter(adapter);
    } else {
      Toast.makeText(this, "No forecast data available.", Toast.LENGTH_SHORT).show();
    }
  }

  private String formatDate(String date) {
    if (date == null || date.isEmpty()) {
      Log.e("FormatDate", "Date is null or empty");
      return "N/A";
    }
    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
    try {
      return outputFormat.format(inputFormat.parse(date));
    } catch (ParseException e) {
      Log.e("FormatDate", "Failed to parse date: " + date, e);
      return date; // Fallback to the original date
    }
  }


  private int getWeatherIcon(String description) {
    if (description == null) return R.drawable.ic_unknown;

    String desc = description.toLowerCase();
    if (desc.contains("rain")) {
      return R.drawable.ic_rain;
    } else if (desc.contains("sun")) {
      return R.drawable.ic_sunny;
    } else if (desc.contains("cloud")) {
      return R.drawable.ic_cloud;
    } else if (desc.contains("snow")) {
      return R.drawable.ic_snow;
    } else if (desc.contains("storm")) {
      return R.drawable.ic_thunderstorm;
    } else if (desc.contains("mist")) {
      return R.drawable.ic_mist;
    }

    return R.drawable.ic_unknown;
  }



}
