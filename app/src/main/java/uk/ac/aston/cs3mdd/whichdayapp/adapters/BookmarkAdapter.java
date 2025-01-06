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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import uk.ac.aston.cs3mdd.whichdayapp.FavoritesActivity;
import uk.ac.aston.cs3mdd.whichdayapp.MainActivity;
import uk.ac.aston.cs3mdd.whichdayapp.R;
import uk.ac.aston.cs3mdd.whichdayapp.database.AppDatabase;
import uk.ac.aston.cs3mdd.whichdayapp.database.Bookmark;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder> {

  private final List<Bookmark> bookmarks; // Original list
  private final List<Bookmark> filteredBookmarks; // Filtered list
  private final Context context;

  public BookmarkAdapter(List<Bookmark> bookmarks, Context context) {
    this.bookmarks = bookmarks != null ? new ArrayList<>(bookmarks) : new ArrayList<>();
    this.filteredBookmarks = new ArrayList<>(this.bookmarks); // Start with full list
    this.context = context;
  }

  public void filter(String query) {
    filteredBookmarks.clear();

    if (query.isEmpty()) {
      // If query is empty, reset to original list
      filteredBookmarks.addAll(bookmarks);
    } else {
      // Filter based on the query
      for (Bookmark bookmark : bookmarks) {
        if (bookmark.getCityName().toLowerCase().contains(query.toLowerCase())) {
          filteredBookmarks.add(bookmark);
        }
      }
    }

    notifyDataSetChanged(); // Notify RecyclerView of the changes
  }

  @NonNull
  @Override
  public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.bookmark_item, parent, false);
    return new BookmarkViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
    Bookmark bookmark = filteredBookmarks.get(position);
    holder.cityName.setText(bookmark.getCityName());

    holder.cityName.setOnClickListener(v -> {
      Intent intent = new Intent(context, MainActivity.class);
      intent.putExtra("cityName", bookmark.getCityName());
      context.startActivity(intent);
    });

    holder.removeButton.setOnClickListener(v -> Executors.newSingleThreadExecutor().execute(() -> {
      AppDatabase db = AppDatabase.getInstance(context);
      db.bookmarkDao().deleteByName(bookmark.getCityName());
      ((FavoritesActivity) context).runOnUiThread(() -> {
        bookmarks.remove(bookmark); // Remove from original list
        filteredBookmarks.remove(bookmark); // Remove from filtered list
        notifyDataSetChanged(); // Update RecyclerView
      });
    }));
  }

  @Override
  public int getItemCount() {
    return filteredBookmarks.size();
  }

  public void updateList(List<Bookmark> newBookmarks) {
    this.bookmarks.clear();
    this.bookmarks.addAll(newBookmarks);
    this.filteredBookmarks.clear();
    this.filteredBookmarks.addAll(newBookmarks);
    notifyDataSetChanged();
  }

  public static class BookmarkViewHolder extends RecyclerView.ViewHolder {
    TextView cityName;
    Button removeButton;

    public BookmarkViewHolder(@NonNull View itemView) {
      super(itemView);
      cityName = itemView.findViewById(R.id.bookmark_city_name);
      removeButton = itemView.findViewById(R.id.button_remove_bookmark);
    }
  }
}
