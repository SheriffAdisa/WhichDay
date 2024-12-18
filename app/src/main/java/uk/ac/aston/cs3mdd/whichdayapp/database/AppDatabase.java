package uk.ac.aston.cs3mdd.whichdayapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import uk.ac.aston.cs3mdd.whichdayapp.models.FavoriteCity;

@Database(entities = {FavoriteCity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
  private static AppDatabase instance;

  public abstract uk.ac.aston.cs3mdd.whichdayapp.database.FavoriteCityDao favoriteCityDao();

  public static synchronized AppDatabase getInstance(Context context) {
    if (instance == null) {
      instance = Room.databaseBuilder(context.getApplicationContext(),
                      AppDatabase.class, "favorites_database")
              .fallbackToDestructiveMigration()
              .build();
    }
    return instance;
  }
}
