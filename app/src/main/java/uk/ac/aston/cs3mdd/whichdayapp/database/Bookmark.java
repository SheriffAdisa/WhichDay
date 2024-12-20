package uk.ac.aston.cs3mdd.whichdayapp.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents a bookmark in the database.
 */
@Entity(tableName = "bookmarks") // Marks this class as a database entity (table)
public class Bookmark {

  @PrimaryKey
  @NonNull
  private String name; // The city name (used as a unique key)

  // Constructor
  public Bookmark(@NonNull String name) {
    this.name = name;
  }

  // Getter for the name
  public String getName() {
    return name;
  }

  // Setter for the name
  public void setName(@NonNull String name) {
    this.name = name;
  }
}
