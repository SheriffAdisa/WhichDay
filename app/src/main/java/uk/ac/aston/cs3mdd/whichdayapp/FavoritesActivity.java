package uk.ac.aston.cs3mdd.whichdayapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
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

    // RecyclerView setup
    bookmarksRecyclerView = findViewById(R.id.bookmarksRecyclerView);
    bookmarksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    bookmarkAdapter = new BookmarkAdapter(new ArrayList<>(), this);
    bookmarksRecyclerView.setAdapter(bookmarkAdapter);

    // Load bookmarks
    loadBookmarks();
  }


  /**
   * Loads the list of bookmarks from the database and sets up the RecyclerView.
   */
  private void loadBookmarks() {
    Executors.newSingleThreadExecutor().execute(() -> {
      AppDatabase db = AppDatabase.getInstance(this);
      List<Bookmark> bookmarks = db.bookmarkDao().getAllBookmarks();

      // Update the UI on the main thread
      runOnUiThread(() -> {
        if (bookmarks.isEmpty()) {
          Toast.makeText(this, "No bookmarks found", Toast.LENGTH_SHORT).show();
        } else {
          bookmarkAdapter = new BookmarkAdapter(bookmarks, this);
          bookmarksRecyclerView.setAdapter(bookmarkAdapter);
        }
      });
    });
  }



  /**
   * Handles the toolbar's back button action.
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish(); // Close the activity when the back button is pressed
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
