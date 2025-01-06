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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import uk.ac.aston.cs3mdd.whichdayapp.models.DaySummary;
import uk.ac.aston.cs3mdd.whichdayapp.models.WeatherItem;
import uk.ac.aston.cs3mdd.whichdayapp.models.WeatherResponse;

public class MainActivity extends AppCompatActivity {

  private static final String BASE_URL = "https://api.openweathermap.org/";
  private static final String API_KEY = "796b2ffe49982b3d99a31e32d87ff3ef";

  private EditText editTextCity;
  private Button buttonFetchWeather;
  private ProgressBar progressBar;
  private LinearLayout recentSearchesContainer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    editTextCity = findViewById(R.id.editTextCity);
    buttonFetchWeather = findViewById(R.id.buttonFetchWeather);
    progressBar = findViewById(R.id.progressBar);
    recentSearchesContainer = findViewById(R.id.recentSearchesContainer);

    buttonFetchWeather.setOnClickListener(view -> fetchWeatherData());

    String bookmarkedCity = getIntent().getStringExtra("cityName");
    if (bookmarkedCity != null) {
      editTextCity.setText(bookmarkedCity);
      fetchWeatherData();
    }

    updateRecentSearchesUI();
  }

  private void fetchWeatherData() {
    // Get the user input from the EditText, trimming any extra spaces
    String cityNameInput = editTextCity.getText().toString().trim();

    // Check if the city name input is empty and notify the user
    if (cityNameInput.isEmpty()) {
      Toast.makeText(this, "City name cannot be empty.", Toast.LENGTH_SHORT).show();
      return; // Stop execution if input is invalid
    }

    // Show a progress bar to indicate that the API call is in progress
    progressBar.setVisibility(View.VISIBLE);

    // Initialize Retrofit instance for making network requests
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL) // Set the base URL for the API
            .addConverterFactory(GsonConverterFactory.create()) // Add a Gson converter for JSON parsing
            .build();

    // Create an instance of the WeatherApi interface
    WeatherApi weatherApi = retrofit.create(WeatherApi.class);

    // Prepare the API call with the user-provided city name and API key
    Call<WeatherResponse> call = weatherApi.getWeatherByCityName(cityNameInput, API_KEY);

    // Execute the API call asynchronously
    call.enqueue(new Callback<WeatherResponse>() {
      @Override
      public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
        // Hide the progress bar once the response is received
        progressBar.setVisibility(View.GONE);

        // Check if the response is successful and contains valid data
        if (response.isSuccessful() && response.body() != null) {
          // Extract data from the API response
          WeatherResponse weatherResponse = response.body();
          double cityLat = weatherResponse.getCity().getCoord().getLat();
          double cityLon = weatherResponse.getCity().getCoord().getLon();
          String cityName = weatherResponse.getCity().getName();

          // Process the weather data: group forecasts by day and calculate summaries
          List<DaySummary> summaries = calculateDailySummaries(groupForecastsByDay(weatherResponse.getList()));
          DaySummary bestDay = getBestDay(summaries); // Determine the best day for activities

          // Prepare an Intent to navigate to the WeatherDetailsActivity
          Intent intent = new Intent(MainActivity.this, WeatherDetailsActivity.class);
          intent.putExtra("cityLat", cityLat); // Pass the city's latitude
          intent.putExtra("cityLon", cityLon); // Pass the city's longitude
          intent.putExtra("cityName", cityName); // Pass the city's name

          // Pass recommendation details if available, otherwise pass defaults
          if (bestDay != null) {
            intent.putExtra("recommendedDay", bestDay.getDate());
            intent.putExtra("recommendedDescription", bestDay.getDescription());
            intent.putExtra("recommendedTemp", bestDay.getAvgTemp());
          } else {
            intent.putExtra("recommendedDay", "N/A");
            intent.putExtra("recommendedDescription", "No recommendation available.");
            intent.putExtra("recommendedTemp", 0.0);
          }

          // Attach the daily summaries to the Intent
          intent.putParcelableArrayListExtra("summaries", new ArrayList<>(summaries));

          // Start the WeatherDetailsActivity with the prepared Intent
          startActivity(intent);

          // Save the city name to the recent searches list for future reference
          saveToRecentSearches(cityNameInput);
        } else {
          // Notify the user if the city name is invalid or data is unavailable
          Toast.makeText(MainActivity.this, "Invalid city name. Please try again.", Toast.LENGTH_SHORT).show();
        }
      }

      @Override
      public void onFailure(Call<WeatherResponse> call, Throwable t) {
        // Hide the progress bar and show an error message if the API request fails
        progressBar.setVisibility(View.GONE);
        Toast.makeText(MainActivity.this, "API request failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
      }
    });
  }


  private List<String> getRecentSearches() {
    SharedPreferences prefs = getSharedPreferences("RecentSearchesPrefs", MODE_PRIVATE);
    Set<String> recentSearches = prefs.getStringSet("recentSearches", new LinkedHashSet<>());

    // Convert Set to List and reverse to show latest first
    List<String> recentList = new ArrayList<>(recentSearches);
    Collections.reverse(recentList);
    return recentList;
  }

  private void saveToRecentSearches(String cityName) {
    SharedPreferences prefs = getSharedPreferences("RecentSearchesPrefs", MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();

    // Get existing searches
    Set<String> recentSearches = prefs.getStringSet("recentSearches", new LinkedHashSet<>());

    // Ensure the city hasn't been said before and is at the top
    if (recentSearches.contains(cityName)) {
      recentSearches.remove(cityName); // Remove duplicates
    }
    recentSearches.add(cityName); // Add new search to the set

    // Save back to SharedPreferences
    editor.putStringSet("recentSearches", recentSearches);
    editor.apply();

    // Update the UI
    updateRecentSearchesUI();
  }

  private void updateRecentSearchesUI() {
    if (recentSearchesContainer == null) {
      Log.e("MainActivity", "recentSearchesContainer is null. Skipping update.");
      return;
    }

    // Clear all dynamically added views
    recentSearchesContainer.removeAllViews();

    // Add the "Recently Searched" label at the top
    TextView label = new TextView(this);
    label.setText("Recently Searched:");
    label.setTextSize(28);
    label.setTypeface(null, android.graphics.Typeface.BOLD); // Make the text bold
    label.setTextColor(getResources().getColor(android.R.color.white));
    label.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    label.setGravity(Gravity.CENTER);


    // Add margin below the label
    LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
    );
    labelParams.setMargins(0, 0, 0, 20); // Add space below the label
    label.setLayoutParams(labelParams);

    recentSearchesContainer.addView(label);

    // Get the list of recent searches
    List<String> recentSearches = getRecentSearches();

    for (String city : recentSearches) {
      // Dynamically create TextViews for each recent search
      TextView textView = new TextView(this);
      textView.setText(city);
      textView.setTextSize(24);
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
      params.setMargins(0, 12, 0, 12); // Top and bottom margin
      textView.setLayoutParams(params);

      // Add the TextView to the container
      recentSearchesContainer.addView(textView);
    }
  }


  public static Map<String, List<WeatherItem>> groupForecastsByDay(List<WeatherItem> forecastList) {
    Map<String, List<WeatherItem>> dailyForecasts = new HashMap<>();
    for (WeatherItem item : forecastList) {
      String date = item.getDt_txt().split(" ")[0];
      dailyForecasts.putIfAbsent(date, new ArrayList<>());
      dailyForecasts.get(date).add(item);
    }
    return dailyForecasts;
  }

  public static List<DaySummary> calculateDailySummaries(Map<String, List<WeatherItem>> dailyForecasts) {
    List<DaySummary> summaries = new ArrayList<>();
    for (Map.Entry<String, List<WeatherItem>> entry : dailyForecasts.entrySet()) {
      String date = entry.getKey();
      List<WeatherItem> items = entry.getValue();

      double avgTemp = items.stream()
              .mapToDouble(item -> item.getMain().getTemp())
              .average()
              .orElse(0.0);

      String description = items.get(0).getWeather().get(0).getDescription();
      summaries.add(new DaySummary(date, avgTemp - 273.15, description));
    }
    return summaries;
  }

  public static DaySummary getBestDay(List<DaySummary> summaries) {
    return summaries.stream()
            .max(Comparator.comparingDouble(DaySummary::getAvgTemp))
            .orElse(null);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.menu_bookmarks) {
      Intent intent = new Intent(this, FavoritesActivity.class);
      startActivity(intent);
      return true;
    } else if (item.getItemId() == R.id.menu_view_map) {
      Intent intent = new Intent(this, MapActivity.class);
      startActivity(intent);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
