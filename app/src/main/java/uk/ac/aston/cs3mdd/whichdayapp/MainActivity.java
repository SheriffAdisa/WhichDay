package uk.ac.aston.cs3mdd.whichdayapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uk.ac.aston.cs3mdd.whichdayapp.models.WeatherResponse;

public class MainActivity extends AppCompatActivity {

  // Base URL and API key for OpenWeatherMap
  private static final String BASE_URL = "https://api.openweathermap.org/";
  private static final String API_KEY = "796b2ffe49982b3d99a31e32d87ff3ef";

  // UI Components
  private EditText editTextCity; // Input field for the user to type the city name
  private Button buttonFetchWeather; // Button to fetch weather data
  private TextView weatherResult; // TextView to display weather data

  private WeatherApi weatherApi; // Interface for Retrofit API calls

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main); // Connects the activity to its XML layout

    // Initialize UI components
    editTextCity = findViewById(R.id.editTextCity);
    buttonFetchWeather = findViewById(R.id.buttonFetchWeather);
    weatherResult = findViewById(R.id.weatherResult);

    // Set up Retrofit instance
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Converts JSON into Java objects
            .build();
    weatherApi = retrofit.create(WeatherApi.class);

    // Set up a button click listener to fetch weather data
    buttonFetchWeather.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        fetchWeatherData(); // Trigger data fetching when the button is clicked
      }
    });
  }

  /**
   * Fetch weather data for the city entered by the user.
   */
  private void fetchWeatherData() {
    String cityName = editTextCity.getText().toString().trim(); // Get the city name and remove extra spaces

    // Check if the city name is empty
    if (cityName.isEmpty()) {
      weatherResult.setText("Please enter a city name.");
      return; // Exit the method early if input is invalid
    }

    try {
      // Create an API call to fetch weather data
      Call<WeatherResponse> call = weatherApi.getWeatherByCityName(cityName, API_KEY, "metric");

      // Execute the API call asynchronously
      call.enqueue(new Callback<WeatherResponse>() {
        @Override
        public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
          if (response.isSuccessful() && response.body() != null) {
            // Parse the API response
            WeatherResponse weatherResponse = response.body();

            // Check if city and weather data are valid
            if (weatherResponse.getCity() != null && !weatherResponse.getList().isEmpty()) {
              // Extract weather data
              String city = weatherResponse.getCity().getName();
              String description = weatherResponse.getList().get(0).getWeather().get(0).getDescription();
              double temp = weatherResponse.getList().get(0).getMain().getTemp();
              String date = weatherResponse.getList().get(0).getDt_txt();

              // Format and display the result
              String result = "City: " + city +
                      "\nTemperature: " + temp + "°C" + // Change K to °C if needed
                      "\nDescription: " + description +
                      "\nDate: " + date;
              weatherResult.setText(result);
              Log.d("API Response", response.body().toString());
              Log.d("API Call", call.request().url().toString());
            } else {
              weatherResult.setText("Invalid data received from the server.");
            }
          } else {
            weatherResult.setText("Error fetching data. " +
                    "Please check the city name or try again later.");
          }


        }

        @Override
        public void onFailure(Call<WeatherResponse> call, Throwable t) {
          // Handle network errors
          weatherResult.setText("Network error: " + t.getMessage() +
                  "\nPlease check your connection.");
        }
      });
    } catch (Exception e) {
      // Catch any unexpected errors
      weatherResult.setText("Unexpected error: " + e.getMessage());
    }
  }
}
