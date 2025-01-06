package uk.ac.aston.cs3mdd.whichdayapp;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//retrofit instance for making API calls
public class RetrofitClient {

  private static Retrofit retrofit = null;

  public static Retrofit getClient() {
    if (retrofit == null) {
      retrofit = new Retrofit.Builder()
              .baseUrl("https://api.openweathermap.org/")  // API base URL
              .addConverterFactory(GsonConverterFactory.create())
              .build();
    }
    return retrofit;
  }
}
