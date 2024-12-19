package uk.ac.aston.cs3mdd.whichdayapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uk.ac.aston.cs3mdd.whichdayapp.models.DaySummary;
import uk.ac.aston.cs3mdd.whichdayapp.models.WeatherItem;
import uk.ac.aston.cs3mdd.whichdayapp.models.WeatherResponse;

public class MainActivity extends AppCompatActivity {

  // OpenWeatherMap API base URL and key
  private static final String BASE_URL = "https://api.openweathermap.org/";
  private static final String API_KEY = "796b2ffe49982b3d99a31e32d87ff3ef";

  // UI components
  private EditText editTextCity;        // User input for city name
  private Button buttonFetchWeather;   // Button to fetch weather data
  private ProgressBar progressBar;     // Spinner to indicate loading



  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Link the Toolbar
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);


    // Linking UI components to XML
    editTextCity = findViewById(R.id.editTextCity);
    buttonFetchWeather = findViewById(R.id.buttonFetchWeather);
    progressBar = findViewById(R.id.progressBar);

    // Set up button click listener
    buttonFetchWeather.setOnClickListener(view -> fetchWeatherData());


  }

  // Fetch weather data from OpenWeatherMap API
  private void fetchWeatherData() {
    String cityName = editTextCity.getText().toString().trim();

    if (!cityName.isEmpty()) {
      progressBar.setVisibility(View.VISIBLE); // Show progress bar during API call

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
          progressBar.setVisibility(View.GONE); // Hide progress bar

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
            Log.e("API Error", "You entered an invalid city! Please try again.");
          }
        }

        @Override
        public void onFailure(Call<WeatherResponse> call, Throwable t) {
          progressBar.setVisibility(View.GONE); // Hide progress bar
          Log.e("API Error", "Failure: " + t.getMessage());
        }
      });
    } else {
      Log.e("Input Error", "City name cannot be empty");
    }
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
      summaries.add(new DaySummary(date, avgTemp - 273.15, description)); // Kelvin to Celsius
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
    }
    return super.onOptionsItemSelected(item);
  }
}
