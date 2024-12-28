package uk.ac.aston.cs3mdd.whichdayapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.Executors;

import uk.ac.aston.cs3mdd.whichdayapp.FavoritesActivity;
import uk.ac.aston.cs3mdd.whichdayapp.MainActivity;
import uk.ac.aston.cs3mdd.whichdayapp.R;
import uk.ac.aston.cs3mdd.whichdayapp.database.AppDatabase;
import uk.ac.aston.cs3mdd.whichdayapp.database.Bookmark;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder> {

  private List<Bookmark> bookmarks; // List of bookmarks to display
  private Context context; // Context for inflating views and handling navigation

  // Constructor to initialize the adapter
  public BookmarkAdapter(List<Bookmark> bookmarks, Context context) {
    this.bookmarks = bookmarks;
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

    // Handle clicking the city name
    holder.cityName.setOnClickListener(v -> {
      Intent intent = new Intent(context, MainActivity.class);
      intent.putExtra("cityName", bookmark.getCityName());
      context.startActivity(intent);
    });

    // Handle clicking the remove button
    holder.removeButton.setOnClickListener(v -> {
      Executors.newSingleThreadExecutor().execute(() -> {
        AppDatabase db = AppDatabase.getInstance(context);
        db.bookmarkDao().deleteByName(bookmark.getCityName());

        // Update UI on the main thread
        ((FavoritesActivity) context).runOnUiThread(() -> {
          bookmarks.remove(position); // Remove from the list
          notifyItemRemoved(position); // Notify RecyclerView
          notifyItemRangeChanged(position, bookmarks.size()); // Update remaining items
        });
      });
    });
  }

  @Override
  public int getItemCount() {
    return bookmarks.size();
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
