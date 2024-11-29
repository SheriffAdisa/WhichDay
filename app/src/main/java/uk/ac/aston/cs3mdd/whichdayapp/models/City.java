package uk.ac.aston.cs3mdd.whichdayapp.models;

public class City {
  private String name; // Maps the "name" field
  private Coord coord; // Maps the "coord" object
  private String country; // Maps the "country" field

  public String getName() {
    return name;
  }

  public Coord getCoord() {
    return coord;
  }

  public String getCountry() {
    return country;
  }
}
