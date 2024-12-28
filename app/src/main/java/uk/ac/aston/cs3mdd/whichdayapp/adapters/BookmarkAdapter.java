package uk.ac.aston.cs3mdd.whichdayapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.Executors;

import uk.ac.aston.cs3mdd.whichdayapp.FavoritesActivity;
import uk.ac.aston.cs3mdd.whichdayapp.R;
import uk.ac.aston.cs3mdd.whichdayapp.database.AppDatabase;
import uk.ac.aston.cs3mdd.whichdayapp.database.Bookmark;


public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder> {

  private List<Bookmark> bookmarks;
  private Context context;

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

    // Handle the remove button click
    holder.removeButton.setOnClickListener(v -> {
      Executors.newSingleThreadExecutor().execute(() -> {
        AppDatabase db = AppDatabase.getInstance(context);
        db.bookmarkDao().deleteByName(bookmark.getCityName());

        // Update UI on main thread
        ((FavoritesActivity) context).runOnUiThread(() -> {
          bookmarks.remove(position);
          notifyItemRemoved(position);
          notifyItemRangeChanged(position, bookmarks.size());
        });
      });
    });
  }


  @Override
  public int getItemCount() {
    return bookmarks.size();
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
