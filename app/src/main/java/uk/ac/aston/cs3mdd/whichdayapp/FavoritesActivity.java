package uk.ac.aston.cs3mdd.whichdayapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import uk.ac.aston.cs3mdd.whichdayapp.adapters.BookmarkAdapter;
import uk.ac.aston.cs3mdd.whichdayapp.database.AppDatabase;
import uk.ac.aston.cs3mdd.whichdayapp.database.Bookmark;

public class FavoritesActivity extends AppCompatActivity {

  private RecyclerView bookmarksRecyclerView; // RecyclerView for bookmarks
  private BookmarkAdapter bookmarkAdapter;    // Adapter for RecyclerView
  private List<Bookmark> bookmarks;           // List of bookmarks from the database

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_favorites);

    // Toolbar setup
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle("Bookmarks");
    }
    // Handle back navigation
    toolbar.setNavigationOnClickListener(v -> onBackPressed());

    // RecyclerView setup
    bookmarksRecyclerView = findViewById(R.id.bookmarksRecyclerView);
    bookmarksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    // Initialize adapter with an empty list
    bookmarks = new ArrayList<>();
    bookmarkAdapter = new BookmarkAdapter(bookmarks, this);
    bookmarksRecyclerView.setAdapter(bookmarkAdapter);

    // Load bookmarks
    loadBookmarks();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_favorites, menu); // Ensure menu_favorites exists
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    int itemId = item.getItemId();

    if (itemId == R.id.view_map) {
      Intent intent = new Intent(this, MapActivity.class);
      startActivity(intent);
      return true;
    } else if (itemId == R.id.sort_alphabetically) {
      sortBookmarksAlphabetically();
      return true;
    } else if (itemId == R.id.sort_by_date) {
      sortBookmarksByDate();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }


  /**
   * Sorts bookmarks alphabetically by city name.
   */
  private void sortBookmarksAlphabetically() {
    Collections.sort(bookmarks, (b1, b2) -> b1.getCityName().compareToIgnoreCase(b2.getCityName()));
    bookmarkAdapter.notifyDataSetChanged();
  }

  /**
   * Sorts bookmarks by creation date (descending order).
   */
  private void sortBookmarksByDate() {
    Collections.sort(bookmarks, (b1, b2) -> Long.compare(b2.getId(), b1.getId())); // Descending by ID
    bookmarkAdapter.notifyDataSetChanged();
  }

  /**
   * Loads the list of bookmarks from the database and sets up the RecyclerView.
   */
  private void loadBookmarks() {
    Executors.newSingleThreadExecutor().execute(() -> {
      try {
        AppDatabase db = AppDatabase.getInstance(this);
        List<Bookmark> fetchedBookmarks = db.bookmarkDao().getAllBookmarks();

        // Update the UI on the main thread
        runOnUiThread(() -> {
          bookmarks.clear(); // Clear the current list
          bookmarks.addAll(fetchedBookmarks); // Add new items
          bookmarkAdapter.notifyDataSetChanged(); // Notify adapter about data change

          if (fetchedBookmarks.isEmpty()) {
            Toast.makeText(this, "No bookmarks found", Toast.LENGTH_SHORT).show();
          }
        });
      } catch (Exception e) {
        runOnUiThread(() -> Toast.makeText(this, "Failed to load bookmarks.", Toast.LENGTH_SHORT).show());
        e.printStackTrace(); // Log the error for debugging
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    loadBookmarks(); // Refresh data when returning to the activity
  }
}
