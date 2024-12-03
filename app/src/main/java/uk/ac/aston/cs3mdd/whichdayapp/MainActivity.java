package uk.ac.aston.cs3mdd.whichdayapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

  // OpenWeatherMap API base URL and key
  private static final String BASE_URL = "https://api.openweathermap.org/";
  private static final String API_KEY = "796b2ffe49982b3d99a31e32d87ff3ef";

  // UI components
  private EditText editTextCity;        // Input for the user to type the city name
  private Button buttonFetchWeather;   // Button to trigger weather fetching
  private ProgressBar progressBar;     // Spinner to indicate data is loading
  private ListView forecastListView;   // ListView to display the 5-day forecast
  private TextView recommendedDayView; // TextView to display the recommended day

  // Google Map instance
  private GoogleMap mMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Linking UI components to XML elements
    editTextCity = findViewById(R.id.editTextCity);
    buttonFetchWeather = findViewById(R.id.buttonFetchWeather);
    progressBar = findViewById(R.id.progressBar);
    recommendedDayView = findViewById(R.id.recommendedDay);
    forecastListView = findViewById(R.id.forecastList);

    // Setting up the Google Map fragment
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.mapFragment);
    if (mapFragment != null) {
      // Async call to initialize the map
      mapFragment.getMapAsync(this);
    }

    // Set up a click listener for the "Get Weather" button
    buttonFetchWeather.setOnClickListener(view -> fetchWeatherData());
  }

  // Called when the Google Map is fully initialized and ready to be used
  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap; // Storing the map instance for later use
  }

  // This method fetches weather data from the OpenWeatherMap API
  private void fetchWeatherData() {
    // Get the city name entered by the user
    String cityName = editTextCity.getText().toString().trim();

    // Ensure the user has entered a valid city name
    if (!cityName.isEmpty()) {
      // Show the loading spinner
      progressBar.setVisibility(View.VISIBLE);

      // Set up Retrofit for API calls
      Retrofit retrofit = new Retrofit.Builder()
              .baseUrl(BASE_URL) // Base URL for the API
              .addConverterFactory(GsonConverterFactory.create()) // JSON parsing
              .build();

      // Create an instance of the API interface
      WeatherApi weatherApi = retrofit.create(WeatherApi.class);

      // Make the API call to fetch weather data for the specified city
      Call<WeatherResponse> call = weatherApi.getWeatherByCityName(cityName, API_KEY);
      call.enqueue(new Callback<WeatherResponse>() {
        @Override
        public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
          // Hide the loading spinner once we have a response
          progressBar.setVisibility(View.GONE);

          if (response.isSuccessful() && response.body() != null) {
            // Parse the weather data from the API response
            WeatherResponse weatherResponse = response.body();
            Log.d("API Response", response.body().toString());

            // Update the Google Map with the city's location
            updateMapWithCity(weatherResponse);

            // Group forecasts by day and process them
            Map<String, List<WeatherItem>> dailyForecasts = groupForecastsByDay(weatherResponse.getList());
            List<DaySummary> summaries = calculateDailySummaries(dailyForecasts);

            // Find the best day based on the weather data
            DaySummary bestDay = getBestDay(summaries);

            // Display the forecast and recommended day in the UI
            displayWeatherData(summaries, bestDay);
          } else {
            // Show an error message if the response wasn't successful
            recommendedDayView.setText("Error: Unable to fetch weather data.");
            Log.e("API Error", "Response failed: " + response.errorBody());
          }
        }

        @Override
        public void onFailure(Call<WeatherResponse> call, Throwable t) {
          // Hide the spinner and show a network error message
          progressBar.setVisibility(View.GONE);
          recommendedDayView.setText("Network error: " + t.getMessage());
        }
      });
    } else {
      // Prompt the user to enter a city name
      recommendedDayView.setText("Please enter a city name.");
    }
  }

  private String formatDate(String inputDate) {
    try {
      // Parse the input date string into a Date object
      SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
      Date date = inputFormat.parse(inputDate);

      // Format the Date object into the desired format
      SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yy");
      return outputFormat.format(date);
    } catch (ParseException e) {
      e.printStackTrace();
      return inputDate; // Return the original date if parsing fails
    }
  }


  // This method updates the Google Map with a marker for the city's location
  private void updateMapWithCity(WeatherResponse weatherResponse) {
    if (mMap != null) {
      // Clear any existing markers on the map
      mMap.clear();

      // Get the city's latitude, longitude, and name
      double lat = weatherResponse.getCity().getCoord().getLat();
      double lon = weatherResponse.getCity().getCoord().getLon();
      String cityName = weatherResponse.getCity().getName();

      // Create a LatLng object for the city's location
      LatLng cityLocation = new LatLng(lat, lon);

      // Add a marker on the map for the city
      mMap.addMarker(new MarkerOptions().position(cityLocation).title("Weather in " + cityName));

      // Move the camera to focus on the city
      mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityLocation, 10));
    } else {
      Log.e("MapError", "Map or city coordinates are null.");
    }
  }

  // Groups forecast items by their date
  private Map<String, List<WeatherItem>> groupForecastsByDay(List<WeatherItem> forecastList) {
    Map<String, List<WeatherItem>> dailyForecasts = new HashMap<>();
    for (WeatherItem item : forecastList) {
      String date = item.getDt_txt().split(" ")[0]; // Extract the date portion
      dailyForecasts.putIfAbsent(date, new ArrayList<>());
      dailyForecasts.get(date).add(item);
    }
    return dailyForecasts;
  }

  // Calculates daily weather summaries (average temp and description)
  private List<DaySummary> calculateDailySummaries(Map<String, List<WeatherItem>> dailyForecasts) {
    List<DaySummary> summaries = new ArrayList<>();
    for (Map.Entry<String, List<WeatherItem>> entry : dailyForecasts.entrySet()) {
      String date = entry.getKey();
      List<WeatherItem> items = entry.getValue();

      // Calculate the average temperature for the day
      double avgTemp = items.stream()
              .mapToDouble(item -> item.getMain().getTemp())
              .average()
              .orElse(0.0);

      // Use the first weather description of the day
      String description = items.get(0).getWeather().get(0).getDescription();

      // Create a DaySummary object for the day
      summaries.add(new DaySummary(formatDate(date), avgTemp - 273.15, description)); // Convert Kelvin to Celsius
    }
    return summaries;
  }

  // Finds the day with the highest average temperature
  private DaySummary getBestDay(List<DaySummary> summaries) {
    return summaries.stream()
            .max(Comparator.comparingDouble(DaySummary::getAvgTemp)) // Find the day with the highest temp
            .orElse(null); // Return null if there are no summaries
  }

  // Displays the weather data and the recommended day in the UI
  private void displayWeatherData(List<DaySummary> summaries, DaySummary bestDay) {

    navigateToWeatherDetails(summaries, bestDay);


    // Display the recommended day in the TextView
    if (bestDay != null) {
      recommendedDayView.setText("Recommended Day: " + bestDay.getDate() + " - " + bestDay.getDescription());
    }

    // Populate the ListView with the 5-day forecast
    ArrayAdapter<DaySummary> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, summaries);
    forecastListView.setAdapter(adapter);
  }

  private void navigateToWeatherDetails(List<DaySummary> summaries, DaySummary bestDay) {
    Intent intent = new Intent(MainActivity.this, WeatherDetailsActivity.class);

    // Pass the recommended day
    intent.putExtra("recommendedDay", bestDay.getDate() + " - " + bestDay.getDescription());

    // Pass the list of summaries as a serialized ArrayList
    ArrayList<DaySummary> serializedSummaries = new ArrayList<>(summaries);
    intent.putParcelableArrayListExtra("summaries", serializedSummaries);

    startActivity(intent); // Navigate to the new activity
  }


}
