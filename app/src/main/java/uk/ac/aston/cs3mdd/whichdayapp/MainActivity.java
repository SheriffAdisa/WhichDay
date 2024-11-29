package uk.ac.aston.cs3mdd.whichdayapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uk.ac.aston.cs3mdd.whichdayapp.models.WeatherItem;
import uk.ac.aston.cs3mdd.whichdayapp.models.WeatherResponse;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

  private static final String BASE_URL = "https://api.openweathermap.org/"; // OpenWeatherMap base URL
  private static final String API_KEY = "796b2ffe49982b3d99a31e32d87ff3ef"; // OpenWeatherMap API key

  // UI Components
  private EditText editTextCity; // User input for city name
  private Button buttonFetchWeather; // Button to trigger fetching weather data
  private TextView weatherResult; // Displays the weather data to the user
  private ProgressBar progressBar; // Indicates loading state during API calls

  // Google Map instance
  private GoogleMap mMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Link UI components with their XML counterparts
    editTextCity = findViewById(R.id.editTextCity);
    buttonFetchWeather = findViewById(R.id.buttonFetchWeather);
    weatherResult = findViewById(R.id.weatherResult);
    progressBar = findViewById(R.id.progressBar);

    // Set up the Google Map fragment
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.mapFragment);
    mapFragment.getMapAsync(this);

    // Set up button click listener to fetch weather data
    buttonFetchWeather.setOnClickListener(view -> fetchWeatherData());
  }

  // Called when the Google Map is ready to be used
  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap; // Store the map instance for later use
  }

  // Fetch weather data from the OpenWeatherMap API
  private void fetchWeatherData() {
    String cityName = editTextCity.getText().toString().trim(); // Get user input and trim whitespace

    if (!cityName.isEmpty()) {
      progressBar.setVisibility(View.VISIBLE); // Show the ProgressBar when loading starts

      // Set up Retrofit for API calls
      Retrofit retrofit = new Retrofit.Builder()
              .baseUrl(BASE_URL) // Base URL for OpenWeatherMap API
              .addConverterFactory(GsonConverterFactory.create()) // Gson for JSON parsing
              .build();

      WeatherApi weatherApi = retrofit.create(WeatherApi.class);

      // Make an asynchronous API call to fetch weather data
      Call<WeatherResponse> call = weatherApi.getWeatherByCityName(cityName, API_KEY);

      call.enqueue(new Callback<WeatherResponse>() {
        @Override
        public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
          progressBar.setVisibility(View.GONE); // Hide the ProgressBar when response is received

          if (response.isSuccessful() && response.body() != null) {
            // Parse the response
            WeatherResponse weatherResponse = response.body();

            // Extract city name and coordinates
            String city = weatherResponse.getCity().getName(); // City name
            double lat = weatherResponse.getCity().getCoord().getLat(); // Latitude
            double lon = weatherResponse.getCity().getCoord().getLon(); // Longitude

            // Extract weather details
            String description = weatherResponse.getList().get(0).getWeather().get(0).getDescription(); // Weather description
            double temp = weatherResponse.getList().get(0).getMain().getTemp(); // Temperature in Kelvin
            double tempCelsius = temp - 273.15; // Convert Kelvin to Celsius
            String date = weatherResponse.getList().get(0).getDt_txt(); // Date and time

            // Display weather data in the TextView
            String weatherInfo = "City: " + city +
                    "\nTemperature: " + String.format("%.2f", tempCelsius) + "°C" +
                    "\nCondition: " + description +
                    "\nDate: " + date;

            weatherResult.setText(weatherInfo);

            // Update the map with a marker at the city's location
            LatLng cityLocation = new LatLng(lat, lon); // Create a LatLng object with the coordinates
            mMap.clear(); // Clear any existing markers
            mMap.addMarker(new MarkerOptions().position(cityLocation).title(city)); // Add a marker
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityLocation, 10)); // Move the camera to the marker
          } else {
            // Handle unsuccessful API responses
            weatherResult.setText("Error: Unable to fetch weather data. " + response.message());
          }
        }

        @Override
        public void onFailure(Call<WeatherResponse> call, Throwable t) {
          progressBar.setVisibility(View.GONE); // Hide the ProgressBar on failure
          weatherResult.setText("Network error: " + t.getMessage());
        }
      });
    } else {
      progressBar.setVisibility(View.GONE); // Ensure ProgressBar is hidden for empty input
      weatherResult.setText("Please enter a city name.");
    }
  }

}
