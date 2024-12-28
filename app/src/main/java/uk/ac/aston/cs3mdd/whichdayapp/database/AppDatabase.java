package uk.ac.aston.cs3mdd.whichdayapp.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Bookmark.class}, version = 2, exportSchema = false) // Always include `exportSchema` in production
public abstract class AppDatabase extends RoomDatabase {

  private static volatile AppDatabase INSTANCE;

  // Abstract method for DAO
  public abstract BookmarkDao bookmarkDao();

  // Singleton pattern to create and access the database
  public static AppDatabase getInstance(Context context) {
    if (INSTANCE == null) {
      synchronized (AppDatabase.class) {
        if (INSTANCE == null) {
          INSTANCE = Room.databaseBuilder(
                          context.getApplicationContext(),
                          AppDatabase.class,
                          "app_database" // Your database file name
                  )
                  .fallbackToDestructiveMigration() // Add migrations in production
                  .build();
        }
      }
    }
    return INSTANCE;
  }

  // Migration from version 1 to version 2
  private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
      // Example: Add the `cityName` column if it doesn't exist
      database.execSQL("ALTER TABLE bookmarks ADD COLUMN cityName TEXT");
    }
  };

}
