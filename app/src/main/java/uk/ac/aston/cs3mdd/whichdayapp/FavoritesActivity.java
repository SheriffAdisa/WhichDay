package uk.ac.aston.cs3mdd.whichdayapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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

  private RecyclerView bookmarksRecyclerView;
  private BookmarkAdapter bookmarkAdapter;
  private List<Bookmark> bookmarks;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_favorites);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle("Bookmarks");
    }

    toolbar.setNavigationOnClickListener(v -> onBackPressed());

    bookmarksRecyclerView = findViewById(R.id.bookmarksRecyclerView);
    bookmarksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    bookmarks = new ArrayList<>();
    bookmarkAdapter = new BookmarkAdapter(bookmarks, this);
    bookmarksRecyclerView.setAdapter(bookmarkAdapter);

    SearchView searchView = findViewById(R.id.searchBar);
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        bookmarkAdapter.filter(query);
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        bookmarkAdapter.filter(newText);
        return true;
      }
    });

    loadBookmarks();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_favorites, menu);
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

  private void sortBookmarksAlphabetically() {
    Collections.sort(bookmarks, (b1, b2) -> b1.getCityName().compareToIgnoreCase(b2.getCityName()));
    bookmarkAdapter.notifyDataSetChanged();
  }

  private void sortBookmarksByDate() {
    Collections.sort(bookmarks, (b1, b2) -> Long.compare(b2.getId(), b1.getId()));
    bookmarkAdapter.notifyDataSetChanged();
  }

  private void loadBookmarks() {
    Executors.newSingleThreadExecutor().execute(() -> {
      try {
        AppDatabase db = AppDatabase.getInstance(this);
        List<Bookmark> fetchedBookmarks = db.bookmarkDao().getAllBookmarks();
        runOnUiThread(() -> {
          bookmarks.clear();
          bookmarks.addAll(fetchedBookmarks);
          bookmarkAdapter.updateList(fetchedBookmarks);
        });
      } catch (Exception e) {
        runOnUiThread(() -> Toast.makeText(this, "Failed to load bookmarks.", Toast.LENGTH_SHORT).show());
        e.printStackTrace();
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    loadBookmarks();
  }
}
