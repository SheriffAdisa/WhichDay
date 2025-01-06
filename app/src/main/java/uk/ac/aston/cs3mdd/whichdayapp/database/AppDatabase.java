package uk.ac.aston.cs3mdd.whichdayapp.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import uk.ac.aston.cs3mdd.whichdayapp.database.BookmarkDao;

@Database(entities = {Bookmark.class}, version = 6, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {
  private static volatile AppDatabase instance;

  public abstract BookmarkDao bookmarkDao();

  public static AppDatabase getInstance(Context context) {
    if (instance == null) {
      synchronized (AppDatabase.class) {
        if (instance == null) {
          instance = Room.databaseBuilder(context.getApplicationContext(),
                          AppDatabase.class, "app_database")
                  .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                  .fallbackToDestructiveMigration()
                  .build();
        }
      }
    }
    return instance;
  }


  // Migration from version 1 to 2
  static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
      database.execSQL("ALTER TABLE bookmarks ADD COLUMN latitude REAL DEFAULT NULL");
      database.execSQL("ALTER TABLE bookmarks ADD COLUMN longitude REAL DEFAULT NULL");
    }
  };

  // Migration from version 2 to 3
  static final Migration MIGRATION_2_3 = new Migration(2, 3) {
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
      database.execSQL("UPDATE bookmarks SET latitude = 0.0 WHERE latitude IS NULL");
      database.execSQL("UPDATE bookmarks SET longitude = 0.0 WHERE longitude IS NULL");
    }
  };
  static final Migration MIGRATION_3_4 = new Migration(3, 4) {
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
      database.execSQL(
              "CREATE TABLE IF NOT EXISTS bookmarks_temp (" +
                      "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                      "cityName TEXT NOT NULL, " + // Enforcing NOT NULL for cityName
                      "latitude REAL NOT NULL, " +
                      "longitude REAL NOT NULL)"
      );

      database.execSQL(
              "INSERT INTO bookmarks_temp (id, cityName, latitude, longitude) " +
                      "SELECT id, cityName, latitude, longitude FROM bookmarks"
      );

      database.execSQL("DROP TABLE bookmarks");
      database.execSQL("ALTER TABLE bookmarks_temp RENAME TO bookmarks");
    }
  };

  static final Migration MIGRATION_4_5 = new Migration(4, 5) {
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
      database.execSQL(
              "CREATE TABLE bookmarks_temp (" +
                      "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                      "cityName TEXT NOT NULL, " +
                      "latitude REAL NOT NULL, " +
                      "longitude REAL NOT NULL, " +
                      "recommendedDay TEXT DEFAULT NULL)"
      );
      database.execSQL(
              "INSERT INTO bookmarks_temp (id, cityName, latitude, longitude) " +
                      "SELECT id, cityName, latitude, longitude FROM bookmarks"
      );
      database.execSQL("DROP TABLE bookmarks");
      database.execSQL("ALTER TABLE bookmarks_temp RENAME TO bookmarks");
    }
  };

  static final Migration MIGRATION_5_6 = new Migration(5, 6) {
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
      database.execSQL("ALTER TABLE bookmarks ADD COLUMN recommendedDay TEXT DEFAULT NULL");
    }
  };





}
