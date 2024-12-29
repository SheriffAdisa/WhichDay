//package uk.ac.aston.cs3mdd.whichdayapp.database;
//
//import android.content.Context;
//
//import java.util.List;
//import java.util.concurrent.Executors;
//
//public class FavoritesDatabaseHelper {
//
//  private final AppDatabase database;
//
//  public FavoritesDatabaseHelper(Context context) {
//    this.database = AppDatabase.getInstance(context);
//  }
//
//  /**
//   * Adds a bookmark to the Room database.
//   *
//   * @param name the name of the city to bookmark.
//   * @return true if the bookmark was added successfully, false otherwise.
//   */
//  public boolean addBookmark(String name) {
//    try {
//      Executors.newSingleThreadExecutor().execute(() -> {
//        Bookmark bookmark = new Bookmark(name, 0.0, 0.0); // Default latitude and longitude
//        database.bookmarkDao().insert(bookmark);
//      });
//      return true;
//    } catch (Exception e) {
//      e.printStackTrace();
//      return false;
//    }
//  }
//
//  /**
//   * Removes a bookmark from the Room database.
//   *
//   * @param name the name of the city to remove from bookmarks.
//   * @return true if the bookmark was removed successfully, false otherwise.
//   */
//  public boolean removeBookmark(String name) {
//    try {
//      Executors.newSingleThreadExecutor().execute(() -> {
//        database.bookmarkDao().deleteByName(name);
//      });
//      return true;
//    } catch (Exception e) {
//      e.printStackTrace();
//      return false;
//    }
//  }
//
//  /**
//   * Checks if a bookmark exists in the Room database.
//   *
//   * @param name the name of the city to check.
//   * @return true if the bookmark exists, false otherwise.
//   */
//  public boolean isBookmarked(String name) {
//    try {
//      return database.bookmarkDao().isBookmarked(name);
//    } catch (Exception e) {
//      e.printStackTrace();
//      return false;
//    }
//  }
//
//  /**
//   * Retrieves all bookmarks from the Room database.
//   *
//   * @return a list of all bookmarks.
//   */
//  public List<Bookmark> getAllBookmarks() {
//    try {
//      return database.bookmarkDao().getAllBookmarks();
//    } catch (Exception e) {
//      e.printStackTrace();
//      return null;
//    }
//  }
//}
