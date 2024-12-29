package uk.ac.aston.cs3mdd.whichdayapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//import uk.ac.aston.cs3mdd.whichdayapp.database.FavoritesDatabaseHelper;
import uk.ac.aston.cs3mdd.whichdayapp.models.DaySummary;
import uk.ac.aston.cs3mdd.whichdayapp.models.WeatherItem;
import uk.ac.aston.cs3mdd.whichdayapp.models.WeatherResponse;

public class MainActivity extends AppCompatActivity {

  // OpenWeatherMap API base URL and key
  private static final String BASE_URL = "https://api.openweathermap.org/";
  private static final String API_KEY = "796b2ffe49982b3d99a31e32d87ff3ef";

  // UI components
  private EditText editTextCity;       // User input for city name
  private Button buttonFetchWeather;  // Button to fetch weather data
  private ProgressBar progressBar;    // Spinner to indicate loading
  private LinearLayout recentSearchesContainer; // Container for recent searches

  //private FavoritesDatabaseHelper dbHelper; // SQLite database helper for favorites

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Set up the toolbar
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    // Link UI components to XML
    editTextCity = findViewById(R.id.editTextCity);
    buttonFetchWeather = findViewById(R.id.buttonFetchWeather);
    progressBar = findViewById(R.id.progressBar);
    recentSearchesContainer = findViewById(R.id.recentSearchesContainer);

    // Log errors if critical views are null
    if (progressBar == null) {
      Log.e("MainActivity", "ProgressBar is null. Ensure it exists in the layout.");
    }
    if (recentSearchesContainer == null) {
      Log.e("MainActivity", "recentSearchesContainer is null. Check your layout file.");
    }

    // Initialize the database helper
    //dbHelper = new FavoritesDatabaseHelper(this);

    // Set up button click listener for fetching weather data
    buttonFetchWeather.setOnClickListener(view -> fetchWeatherData());

    // Check if launched from a bookmark
    String bookmarkedCity = getIntent().getStringExtra("cityName");
    if (bookmarkedCity != null) {
      editTextCity.setText(bookmarkedCity);
      fetchWeatherData();
    }

    // Update the UI for recent searches
    if (recentSearchesContainer != null) {
      updateRecentSearchesUI();
    }
  }

  // Fetch weather data from OpenWeatherMap API
  private void fetchWeatherData() {
    String cityName = editTextCity.getText().toString().trim();

    if (cityName.isEmpty()) {
      Toast.makeText(this, "City name cannot be empty.", Toast.LENGTH_SHORT).show();
      return;
    }

    if (progressBar != null) {
      progressBar.setVisibility(View.VISIBLE); // Show progress bar during API call
    }

    // Set up Retrofit for the API call
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    WeatherApi weatherApi = retrofit.create(WeatherApi.class);
    Call<WeatherResponse> call = weatherApi.getWeatherByCityName(cityName, API_KEY);

    call.enqueue(new Callback<WeatherResponse>() {
      @Override
      public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
        if (progressBar != null) {
          progressBar.setVisibility(View.GONE); // Hide progress bar
        }

        if (response.isSuccessful() && response.body() != null) {
          WeatherResponse weatherResponse = response.body();

          // Extract necessary data
          double cityLat = weatherResponse.getCity().getCoord().getLat();
          double cityLon = weatherResponse.getCity().getCoord().getLon();
          String cityName = weatherResponse.getCity().getName();

          // Process weather data
          Map<String, List<WeatherItem>> dailyForecasts = groupForecastsByDay(weatherResponse.getList());
          List<DaySummary> summaries = calculateDailySummaries(dailyForecasts);

          // Find the recommended day
          DaySummary bestDay = getBestDay(summaries);

          // Navigate to WeatherDetailsActivity with the data
          Intent intent = new Intent(MainActivity.this, WeatherDetailsActivity.class);
          intent.putExtra("cityLat", cityLat);
          intent.putExtra("cityLon", cityLon);
          intent.putExtra("cityName", cityName);
          intent.putExtra("recommendedDay", bestDay != null ? bestDay.getDate() + " - " + bestDay.getDescription() : "No recommendation available");
          intent.putParcelableArrayListExtra("summaries", new ArrayList<>(summaries));
          startActivity(intent);
        } else {
          Toast.makeText(MainActivity.this, "Invalid city name. Please try again.", Toast.LENGTH_SHORT).show();
        }
        saveToRecentSearches(cityName);
      }

      @Override
      public void onFailure(Call<WeatherResponse> call, Throwable t) {
        if (progressBar != null) {
          progressBar.setVisibility(View.GONE); // Hide progress bar
        }
        Toast.makeText(MainActivity.this, "API request failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
      }
    });
  }

  // Group forecasts by date
  private Map<String, List<WeatherItem>> groupForecastsByDay(List<WeatherItem> forecastList) {
    Map<String, List<WeatherItem>> dailyForecasts = new HashMap<>();
    for (WeatherItem item : forecastList) {
      String date = item.getDt_txt().split(" ")[0]; // Extract date
      dailyForecasts.putIfAbsent(date, new ArrayList<>());
      dailyForecasts.get(date).add(item);
    }
    return dailyForecasts;
  }

  // Calculate daily summaries
  private List<DaySummary> calculateDailySummaries(Map<String, List<WeatherItem>> dailyForecasts) {
    List<DaySummary> summaries = new ArrayList<>();
    for (Map.Entry<String, List<WeatherItem>> entry : dailyForecasts.entrySet()) {
      String date = entry.getKey();
      List<WeatherItem> items = entry.getValue();

      double avgTemp = items.stream()
              .mapToDouble(item -> item.getMain().getTemp())
              .average()
              .orElse(0.0);

      String description = items.get(0).getWeather().get(0).getDescription();
      summaries.add(new DaySummary(date, avgTemp - 273.15, description)); // Convert Kelvin to Celsius
    }
    return summaries;
  }

  // Get the best day (highest temperature)
  private DaySummary getBestDay(List<DaySummary> summaries) {
    return summaries.stream()
            .max(Comparator.comparingDouble(DaySummary::getAvgTemp))
            .orElse(null);
  }

  // Inflate the menu
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  // Handle menu item clicks
  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.menu_bookmarks) {
      // Navigate to FavoritesActivity
      Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
      startActivity(intent);
      return true;
    } else if (item.getItemId() == R.id.menu_view_map) {
    Intent intent = new Intent(MainActivity.this, MapActivity.class);
    startActivity(intent);
    return true;
  }
    return super.onOptionsItemSelected(item);
  }

  private void saveToRecentSearches(String cityName) {
    SharedPreferences prefs = getSharedPreferences("RecentSearchesPrefs", MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();

    // Get existing searches
    Set<String> recentSearches = prefs.getStringSet("recentSearches", new LinkedHashSet<>());

    // Ensure the city is unique and at the top
    if (recentSearches.contains(cityName)) {
      recentSearches.remove(cityName); // Remove duplicate
    }
    recentSearches.add(cityName); // Add new search to the set

    // Save back to SharedPreferences
    editor.putStringSet("recentSearches", recentSearches);
    editor.apply();

    // Update the UI
    updateRecentSearchesUI();
  }

  private List<String> getRecentSearches() {
    SharedPreferences prefs = getSharedPreferences("RecentSearchesPrefs", MODE_PRIVATE);
    Set<String> recentSearches = prefs.getStringSet("recentSearches", new LinkedHashSet<>());

    // Convert Set to List and reverse to show latest first
    List<String> recentList = new ArrayList<>(recentSearches);
    Collections.reverse(recentList);
    return recentList;
  }

  private void updateRecentSearchesUI() {
    if (recentSearchesContainer == null) {
      Log.e("MainActivity", "recentSearchesContainer is null. Skipping update.");
      return;
    }

    // Get the list of recent searches
    List<String> recentSearches = getRecentSearches();

    // Clear only the dynamically added views (not the "Recently Searched" label)
    int childCount = recentSearchesContainer.getChildCount();
    if (childCount > 1) { // Keep the first child (the "Recently Searched" label)
      recentSearchesContainer.removeViews(1, childCount - 1);
    }

    for (String city : recentSearches) {
      // Dynamically create TextViews for each recent search
      TextView textView = new TextView(this);
      textView.setText(city);
      textView.setTextSize(22);
      textView.setPadding(20, 35, 20, 35);
      textView.setTextColor(getResources().getColor(android.R.color.white)); // Text color
      textView.setBackgroundResource(R.drawable.city_item_background); // Set rounded background
      textView.setGravity(Gravity.CENTER); // Center text
      textView.setOnClickListener(v -> {
        editTextCity.setText(city);
        fetchWeatherData();
      });

      // Add a margin around each city item
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT,
              LinearLayout.LayoutParams.WRAP_CONTENT
      );
      params.setMargins(0, 8, 0, 8); // Top and bottom margin
      textView.setLayoutParams(params);

      // Add the TextView to the container
      recentSearchesContainer.addView(textView);
    }
  }
}
