package uk.ac.aston.cs3mdd.whichdayapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

import java.util.ArrayList;
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
  }

  private void setupMapFragment() {
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.mapFragment);

    if (mapFragment == null) {
      mapFragment = SupportMapFragment.newInstance();
      getSupportFragmentManager()
              .beginTransaction()
              .replace(R.id.mapFragment, mapFragment)
              .commit();
    }

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
        String message = isBookmarked
                ? cityName + " added to bookmarks"
                : cityName + " removed from bookmarks";
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
      Toast.makeText(this, "Error loading map: " + e.getMessage(), Toast.LENGTH_SHORT).show();
      e.printStackTrace();
    }
  }

  private void setListViewHeightBasedOnChildren(ListView listView) {
    ArrayAdapter adapter = (ArrayAdapter) listView.getAdapter();
    if (adapter == null) {
      return;
    }

    int totalHeight = 0;
    for (int i = 0; i < adapter.getCount(); i++) {
      View listItem = adapter.getView(i, null, listView);
      listItem.measure(
              View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.EXACTLY),
              View.MeasureSpec.UNSPECIFIED
      );
      totalHeight += listItem.getMeasuredHeight();
    }

    ViewGroup.LayoutParams params = listView.getLayoutParams();
    params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
    listView.setLayoutParams(params);
    listView.requestLayout();
  }


  private void setupUI() {
    TextView recommendedDayView = findViewById(R.id.recommendedDayView);
    RecyclerView forecastRecyclerView = findViewById(R.id.forecastRecyclerView);

    String recommendedDay = getIntent().getStringExtra("recommendedDay");
    recommendedDayView.setText(recommendedDay != null
            ? "Recommended Day: " + recommendedDay
            : "No recommended day available.");

    ArrayList<DaySummary> summaries = getIntent().getParcelableArrayListExtra("summaries");
    if (summaries != null && !summaries.isEmpty()) {
      ForecastAdapter adapter = new ForecastAdapter(summaries);
      forecastRecyclerView.setLayoutManager(new LinearLayoutManager(this));
      forecastRecyclerView.setAdapter(adapter);
    } else {
      Toast.makeText(this, "No forecast data available.", Toast.LENGTH_SHORT).show();
    }
  }






}
