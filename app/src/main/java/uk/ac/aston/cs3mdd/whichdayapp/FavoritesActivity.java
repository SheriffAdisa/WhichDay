package uk.ac.aston.cs3mdd.whichdayapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uk.ac.aston.cs3mdd.whichdayapp.database.AppDatabase;
import uk.ac.aston.cs3mdd.whichdayapp.database.Bookmark;

public class FavoritesActivity extends AppCompatActivity {

  private ListView listViewFavorites;
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_favorites);
    setTitle("Bookmarked Cities");

    // Set up the toolbar
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    // Enable the back button
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle("Bookmarks");
    }

    // Initialize the ListView
    listViewFavorites = findViewById(R.id.listViewFavorites);

    // Fetch bookmarks from the database
    loadBookmarks();
  }

  private void loadBookmarks() {
    executorService.execute(() -> {
      AppDatabase db = AppDatabase.getInstance(this);
      List<Bookmark> bookmarks = db.bookmarkDao().getAllBookmarks();

      List<String> bookmarkNames = new ArrayList<>();
      for (Bookmark bookmark : bookmarks) {
        bookmarkNames.add(bookmark.getName());
      }

      // Update the UI on the main thread
      runOnUiThread(() -> {
        if (bookmarkNames.isEmpty()) {
          Toast.makeText(this, "No bookmarks found", Toast.LENGTH_SHORT).show();
        } else {
          ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bookmarkNames);
          listViewFavorites.setAdapter(adapter);
        }
      });
    });
  }

  // Handle the toolbar's back button
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish(); // Close the activity
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
