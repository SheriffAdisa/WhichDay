package uk.ac.aston.cs3mdd.whichdayapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import uk.ac.aston.cs3mdd.whichdayapp.models.FavoriteCity;

@Dao
public interface FavoriteCityDao {
  @Insert
  void insertCity(FavoriteCity city);

  @Query("SELECT * FROM favorite_cities ORDER BY id DESC")
  List<FavoriteCity> getAllCities();

  @Delete
  void deleteCity(FavoriteCity city);
}
