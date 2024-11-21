package uk.ac.aston.cs3mdd.whichdayapp;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uk.ac.aston.cs3mdd.whichdayapp.databinding.ActivityMainBinding;
import uk.ac.aston.cs3mdd.whichdayapp.models.WeatherItem;
import uk.ac.aston.cs3mdd.whichdayapp.models.WeatherResponse;
import uk.ac.aston.cs3mdd.whichdayapp.WeatherApi;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

  private static final String BASE_URL = "https://api.openweathermap.org/";
  private static final String API_KEY = "796b2ffe49982b3d99a31e32d87ff3ef"; // Replace with your actual API key


  private EditText editTextCity;
  private Button buttonFetchWeather;
  private TextView weatherResult;
  private WeatherApi weatherApi;

  //on startup
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Initialize UI components
    editTextCity = findViewById(R.id.editTextCity);
    buttonFetchWeather = findViewById(R.id.buttonFetchWeather);
    weatherResult = findViewById(R.id.weatherResult);

    // Set up Retrofit
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    weatherApi = retrofit.create(WeatherApi.class);

    // Set up button click listener
    buttonFetchWeather.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        fetchWeatherData();
      }
    });
  }


  //method to get the weather data
  private void fetchWeatherData() {
    String cityName = editTextCity.getText().toString();

    if (!cityName.isEmpty()) {
      try {
        // Make the API call
        Call<WeatherResponse> call = weatherApi.getWeatherByCityName(cityName, API_KEY);

        call.enqueue(new Callback<WeatherResponse>() {
        @Override
        public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
          if (response.isSuccessful() && response.body() != null) {
            WeatherResponse weatherResponse = response.body();

            if (weatherResponse.getCity() != null && !weatherResponse.getList().isEmpty()) {
              String city = weatherResponse.getCity().getName();
              String description = weatherResponse.getList().get(0).getWeather().get(0).getDescription();
              double temp = weatherResponse.getList().get(0).getMain().getTemp();
              String date = weatherResponse.getList().get(0).getDt_txt();

              String result = "City: " + city +
                      "\nTemperature: " + temp + "K" +
                      "\nDescription: " + description +
                      "\nDate: " + date;

              weatherResult.setText(result);
            } else {
              weatherResult.setText("Invalid data received from the server.");
            }
          } else {
            weatherResult.setText("Error: " + response.message());
          }
        }

        @Override
        public void onFailure(Call<WeatherResponse> call, Throwable t) {
          weatherResult.setText("Network error: " + t.getMessage());
        }
        });
//      } else {
//        weatherResult.setText("Please enter a city name.");
//      }
    } catch (Exception e) {
      weatherResult.setText("Unexpected error: " + e.getMessage());
    }
    }
  }
}