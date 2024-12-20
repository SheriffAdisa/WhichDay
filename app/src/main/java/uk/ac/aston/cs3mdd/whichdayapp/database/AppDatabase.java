package uk.ac.aston.cs3mdd.whichdayapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Represents the main database for the app, using Room persistence library.
 */
@Database(entities = {Bookmark.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

  private static AppDatabase instance;

  // DAO reference
  public abstract BookmarkDao bookmarkDao();

  // Singleton instance of the database
  public static synchronized AppDatabase getInstance(Context context) {
    if (instance == null) {
      instance = Room.databaseBuilder(context.getApplicationContext(),
                      AppDatabase.class, "app_database")
              .fallbackToDestructiveMigration()
              .build();
    }
    return instance;
  }
}
