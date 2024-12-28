package uk.ac.aston.cs3mdd.whichdayapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import uk.ac.aston.cs3mdd.whichdayapp.database.Bookmark;

@Dao
public interface BookmarkDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(Bookmark bookmark);

  @Query("SELECT * FROM bookmarks")
  List<Bookmark> getAllBookmarks();

  @Query("DELETE FROM bookmarks WHERE cityName = :name")
  void deleteByName(String name);

  @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE cityName = :name)")
  boolean isBookmarked(String name);
}
