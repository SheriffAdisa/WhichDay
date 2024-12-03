package uk.ac.aston.cs3mdd.whichdayapp.models;

import android.os.Parcel;
import android.os.Parcelable;


// This class represents a summary of the weather for a single day
public class DaySummary implements Parcelable{
  private String date; // The date of the forecast
  private double avgTemp; // The average temperature for the day
  private String description; // The weather description (e.g., "clear sky")

  // Constructor to initialize the fields
  public DaySummary(String date, double avgTemp, String description) {
    this.date = date;
    this.avgTemp = avgTemp;
    this.description = description;
  }

  protected DaySummary(Parcel in) {
    date = in.readString();
    avgTemp = in.readDouble();
    description = in.readString();
  }

  public static final Creator<DaySummary> CREATOR = new Creator<DaySummary>() {
    @Override
    public DaySummary createFromParcel(Parcel in) {
      return new DaySummary(in);
    }

    @Override
    public DaySummary[] newArray(int size) {
      return new DaySummary[size];
    }
  };


  // Getter for the date
  public String getDate() {
    return date;
  }

  // Getter for the average temperature
  public double getAvgTemp() {
    return avgTemp;
  }

  // Getter for the weather description
  public String getDescription() {
    return description;
  }

  // Override toString to display the day summary in a readable format
  @Override
  public String toString() {
    return date + ": " + String.format("%.2f", avgTemp) + "°C, " + description;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int flags) {
    parcel.writeString(date);
    parcel.writeDouble(avgTemp);
    parcel.writeString(description);
  }
}
