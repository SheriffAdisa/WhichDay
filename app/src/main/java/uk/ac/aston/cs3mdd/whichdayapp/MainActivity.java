package uk.ac.aston.cs3mdd.whichdayapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import uk.ac.aston.cs3mdd.whichdayapp.models.WeatherResponse;
import uk.ac.aston.cs3mdd.whichdayapp.WeatherApi;

public class MainActivity extends AppCompatActivity {

  // Base URL and API Key for OpenWeatherMap API
  private static final String BASE_URL = "https://api.openweathermap.org/";
  private static final String API_KEY = "796b2ffe49982b3d99a31e32d87ff3ef";

  // Declare UI components
  private EditText editTextCity; // EditText for user input (city name)
  private Button buttonFetchWeather; // Button to trigger weather data fetching
  private TextView weatherResult; // TextView to display the fetched weather data
  private ProgressBar progressBar; // ProgressBar to show while data is being fetched

  // Declare the WeatherApi instance to interact with OpenWeatherMap API
  private WeatherApi weatherApi;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main); // Set the layout for the activity

    // Initialize UI components by linking them to their respective IDs in XML
    editTextCity = findViewById(R.id.editTextCity);
    buttonFetchWeather = findViewById(R.id.buttonFetchWeather);
    weatherResult = findViewById(R.id.weatherResult);
    progressBar = findViewById(R.id.progressBar); // ProgressBar for loading indicator

    // Set up Retrofit to communicate with the OpenWeatherMap API
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL) // Set the base URL for the API
            .addConverterFactory(GsonConverterFactory.create()) // Add Gson converter to handle JSON response
            .build(); // Build the Retrofit instance

    // Initialize the WeatherApi interface using Retrofit
    weatherApi = retrofit.create(WeatherApi.class);

    // Set up a click listener for the "Fetch Weather" button
    buttonFetchWeather.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        fetchWeatherData(); // Call the method to fetch weather data
      }
    });
  }

  // Method to fetch weather data based on the city entered by the user
  private void fetchWeatherData() {
    String cityName = editTextCity.getText().toString(); // Get the city name entered by the user

    // Check if the city name is not empty
    if (!cityName.isEmpty()) {
      progressBar.setVisibility(View.VISIBLE); // Show the ProgressBar while fetching data

      try {
        // Make the API call to fetch weather data using Retrofit and the WeatherApi interface
        Call<WeatherResponse> call = weatherApi.getWeatherByCityName(cityName, API_KEY);

        // Enqueue the API call asynchronously
        call.enqueue(new Callback<WeatherResponse>() {
          // Method called when the response is successfully received from the API
          @Override
          public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
            progressBar.setVisibility(View.GONE); // Hide the ProgressBar when the response is received

            // Check if the response is successful and contains a valid body
            if (response.isSuccessful() && response.body() != null) {
              WeatherResponse weatherResponse = response.body(); // Get the weather response object

              // Check if the weather data contains valid city and weather information
              if (weatherResponse.getCity() != null && !weatherResponse.getList().isEmpty()) {
                // Extract weather data for the first forecast entry
                String city = weatherResponse.getCity().getName(); // City name
                String description = weatherResponse.getList().get(0).getWeather().get(0).getDescription(); // Weather description
                double tempKelvin = weatherResponse.getList().get(0).getMain().getTemp(); // Temperature in Kelvin
                double tempCelsius = tempKelvin - 273.15; // Convert temperature from Kelvin to Celsius
                String date = weatherResponse.getList().get(0).getDt_txt(); // Date and time of the forecast

                // Format the result as a string and display it in the weatherResult TextView
                String result = "City: " + city +
                        "\nTemperature: " + String.format("%.2f", tempCelsius) + "°C" +
                        "\nDescription: " + description +
                        "\nDate: " + date;

                weatherResult.setText(result); // Set the formatted weather result to the TextView
              } else {
                // If the response doesn't contain valid weather data
                weatherResult.setText("Invalid data received from the server.");
              }
            } else {
              // If the response wasn't successful, display the error message
              weatherResult.setText("Error: " + response.message());
            }
          }

          // Method called if the API call fails (e.g., no internet connection)
          @Override
          public void onFailure(Call<WeatherResponse> call, Throwable t) {
            progressBar.setVisibility(View.GONE); // Hide the ProgressBar when an error occurs
            weatherResult.setText("Network error: " + t.getMessage()); // Display error message
          }
        });
      } catch (Exception e) {
        progressBar.setVisibility(View.GONE); // Hide the ProgressBar in case of an exception
        weatherResult.setText("Unexpected error: " + e.getMessage()); // Display unexpected error message
      }
    } else {
      // If the user didn't enter a city name, prompt them to do so
      weatherResult.setText("Please enter a city name.");
    }
  }
}
