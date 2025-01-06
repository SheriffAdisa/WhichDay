package uk.ac.aston.cs3mdd.whichdayapp.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bookmarks")
public class Bookmark {

  @PrimaryKey(autoGenerate = true)
  private int id;

  @ColumnInfo(name = "cityName")
  @NonNull
  private String cityName;


  @ColumnInfo(name = "latitude")
  private double latitude;

  @ColumnInfo(name = "longitude")
  private double longitude;


  // Add this field to store the recommended day
  private String recommendedDay;

  // Getters and Setters
  public String getRecommendedDay() {
    return recommendedDay;
  }

  public void setRecommendedDay(String recommendedDay) {
    this.recommendedDay = recommendedDay;
  }


  // Constructor
  public Bookmark(String cityName, double latitude, double longitude) {
    this.cityName = cityName;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  // Getters and setters
  public int getId() { return id; }
  public void setId(int id) { this.id = id; }

  @NonNull
  public String getCityName() { return cityName; }
  public void setCityName(String cityName) { this.cityName = cityName; }

  public double getLatitude() { return latitude; }
  public void setLatitude(double latitude) { this.latitude = latitude; }

  public double getLongitude() { return longitude; }
  public void setLongitude(double longitude) { this.longitude = longitude; }
}
