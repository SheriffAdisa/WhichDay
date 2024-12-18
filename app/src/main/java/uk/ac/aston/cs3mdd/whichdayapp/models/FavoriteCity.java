package uk.ac.aston.cs3mdd.whichdayapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_cities")
public class FavoriteCity {
  @PrimaryKey(autoGenerate = true)
  private int id;
  private String cityName;

  // Constructor
  public FavoriteCity(String cityName) {
    this.cityName = cityName;
  }

  // Getters and Setters
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getCityName() {
    return cityName;
  }

  public void setCityName(String cityName) {
    this.cityName = cityName;
  }
}
