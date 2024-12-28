package uk.ac.aston.cs3mdd.whichdayapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BookmarkDao {

  // Inserts a new bookmark or replaces an existing one with the same primary key
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(Bookmark bookmark);

  // Retrieves all bookmarks, ordered by descending ID
  @Query("SELECT * FROM bookmarks ORDER BY id DESC")
  List<Bookmark> getAllBookmarks();

  // Retrieves a bookmark by its city name
  @Query("SELECT * FROM bookmarks WHERE cityName = :cityName")
  Bookmark getBookmark(String cityName); // Ensure parameter matches ":cityName"

  // Deletes a bookmark by its city name
  @Query("DELETE FROM bookmarks WHERE cityName = :cityName")
  void deleteByName(String cityName); // Ensure parameter matches ":cityName"

  // Checks if a bookmark with a given city name exists
  @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE cityName = :cityName)")
  boolean isBookmarked(String cityName); // Ensure parameter matches ":cityName"
}
