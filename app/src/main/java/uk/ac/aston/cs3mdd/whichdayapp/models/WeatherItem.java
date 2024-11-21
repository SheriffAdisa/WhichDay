package uk.ac.aston.cs3mdd.whichdayapp.models;

import java.util.List;

//This class is amde to list an item
public class WeatherItem {
  private Main main;
  private List<Weather> weather;
  private long dt;
  private Clouds clouds;
  private Wind wind;
  private Rain rain;
  private String dt_txt;

  public Main getMain() {
    return main;
  }

  public List<Weather> getWeather() {
    return weather;
  }

  public long getDt() {
    return dt;
  }

  public Clouds getClouds() {
    return clouds;
  }

  public Wind getWind() {
    return wind;
  }

  public Rain getRain() {
    return rain;
  }

  public String getDt_txt() {
    return dt_txt;
  }
}
