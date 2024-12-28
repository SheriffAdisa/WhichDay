package uk.ac.aston.cs3mdd.whichdayapp.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bookmarks")
public class Bookmark {
  @PrimaryKey(autoGenerate = true)
  private int id;
  private String cityName; // Ensure this field exists

  // Constructor
  public Bookmark(String cityName) {
    this.cityName = cityName;
  }

  // Getter for cityName
  public String getCityName() {
    return cityName;
  }

  // Setter for cityName (optional)
  public void setCityName(String cityName) {
    this.cityName = cityName;
  }

  // Getter for ID (Room requires this)
  public int getId() {
    return id;
  }

  // Setter for ID (optional, Room handles this automatically)
  public void setId(int id) {
    this.id = id;
  }

}
