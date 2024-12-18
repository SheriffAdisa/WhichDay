package uk.ac.aston.cs3mdd.whichdayapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.stream.Collectors;

import uk.ac.aston.cs3mdd.whichdayapp.database.AppDatabase;
import uk.ac.aston.cs3mdd.whichdayapp.models.FavoriteCity;

public class FavoritesActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_favorites);
    setTitle("Favourites");



    // Set up the action bar (toolbar) with a back button
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable back button
    }

    ListView listViewFavorites = findViewById(R.id.listViewFavorites);

    // Load favorites from database
    AppDatabase db = AppDatabase.getInstance(this);
    new Thread(() -> {
      List<FavoriteCity> cities = db.favoriteCityDao().getAllCities();
      List<String> cityNames = cities.stream().map(FavoriteCity::getCityName).collect(Collectors.toList());

      // Update UI on the main thread
      runOnUiThread(() -> {
        if (!cityNames.isEmpty()) {
          ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cityNames);
          listViewFavorites.setAdapter(adapter);
        } else {
          Toast.makeText(this, "No favorites saved yet.", Toast.LENGTH_SHORT).show();
        }
      });
    }).start();
  }


  // Handle the back button press
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) { // Back button ID
      finish(); // Close the current activity and return to the previous one
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
