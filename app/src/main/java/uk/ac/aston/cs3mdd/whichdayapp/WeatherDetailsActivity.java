package uk.ac.aston.cs3mdd.whichdayapp;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import uk.ac.aston.cs3mdd.whichdayapp.models.DaySummary;

public class WeatherDetailsActivity extends AppCompatActivity {



  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_weather_details);

    // Enable the back button in the action bar
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle("Your Recommendation"); // Set title
    }

    // Get references to the UI elements
    TextView recommendedDayView = findViewById(R.id.recommendedDayView);
    ListView forecastListView = findViewById(R.id.forecastListView);

    // Get data from the intent
    String recommendedDay = getIntent().getStringExtra("recommendedDay");
    ArrayList<DaySummary> summaries = getIntent().getParcelableArrayListExtra("summaries");

    // Display the recommended day
    recommendedDayView.setText("Recommended Day: " + recommendedDay);

    // Display the 5-day forecast in the ListView
    ArrayAdapter<DaySummary> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, summaries);
    forecastListView.setAdapter(adapter);


  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Check if the home button (up button) was clicked
    if (item.getItemId() == android.R.id.home) {
      finish(); // Close the current activity and return to the previous one
      return true;
    }
    return super.onOptionsItemSelected(item);
  }


}
