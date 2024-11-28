package uk.ac.aston.cs3mdd.whichdayapp;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import uk.ac.aston.cs3mdd.whichdayapp.models.WeatherResponse;


//This interface was made to define the strucutre of the API calls
public interface WeatherApi {
  //tells retrofit how to call the weather service
  @GET("data/2.5/forecast")  //WeatherResponse would be a Java class that matches the JSON structure returned by the weather API
  Call<WeatherResponse> getWeatherByCityName(
          @Query("q") String cityName,  //user's city
          @Query("appid") String apiKey //my Weather api key
          //converts the temperature to celcius
  );
}
