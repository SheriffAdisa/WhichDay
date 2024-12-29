package uk.ac.aston.cs3mdd.whichdayapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import uk.ac.aston.cs3mdd.whichdayapp.FavoritesActivity;
import uk.ac.aston.cs3mdd.whichdayapp.MainActivity;
import uk.ac.aston.cs3mdd.whichdayapp.R;
import uk.ac.aston.cs3mdd.whichdayapp.database.AppDatabase;
import uk.ac.aston.cs3mdd.whichdayapp.database.Bookmark;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder> {

  private List<Bookmark> bookmarks; // List of bookmarks to display
  private final Context context; // Context for inflating views and handling navigation

  // Constructor to initialize the adapter
  public BookmarkAdapter(List<Bookmark> bookmarks, Context context) {
    this.bookmarks = bookmarks != null ? bookmarks : new ArrayList<>();
    this.context = context;
  }

  @NonNull
  @Override
  public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.bookmark_item, parent, false);
    return new BookmarkViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
    Bookmark bookmark = bookmarks.get(position);

    // Set the city name
    holder.cityName.setText(bookmark.getCityName());

    // Navigate to MainActivity when the city name is clicked
    holder.cityName.setOnClickListener(v -> {
      Intent intent = new Intent(context, MainActivity.class);
      intent.putExtra("cityName", bookmark.getCityName());
      context.startActivity(intent);
    });

    // Handle removing a bookmark
    holder.removeButton.setOnClickListener(v -> {
      Executors.newSingleThreadExecutor().execute(() -> {
        AppDatabase db = AppDatabase.getInstance(context);
        db.bookmarkDao().deleteByName(bookmark.getCityName());

        // Update UI on the main thread
        ((FavoritesActivity) context).runOnUiThread(() -> {
          bookmarks.remove(position); // Remove the bookmark from the list
          notifyItemRemoved(position); // Notify RecyclerView of the removed item
          notifyItemRangeChanged(position, bookmarks.size()); // Update the range of affected items
        });
      });
    });
  }

  @Override
  public int getItemCount() {
    return bookmarks.size();
  }

  /**
   * Updates the bookmark list and refreshes the adapter.
   *
   * @param newBookmarks The new list of bookmarks to display.
   */
  public void updateList(List<Bookmark> newBookmarks) {
    this.bookmarks.clear();
    if (newBookmarks != null) {
      this.bookmarks.addAll(newBookmarks);
    }
    notifyDataSetChanged();
  }

  // ViewHolder for each item in the RecyclerView
  public static class BookmarkViewHolder extends RecyclerView.ViewHolder {
    TextView cityName;
    Button removeButton;

    public BookmarkViewHolder(@NonNull View itemView) {
      super(itemView);
      cityName = itemView.findViewById(R.id.bookmark_city_name); // TextView for city name
      removeButton = itemView.findViewById(R.id.button_remove_bookmark); // Button to remove bookmark
    }
  }
}
