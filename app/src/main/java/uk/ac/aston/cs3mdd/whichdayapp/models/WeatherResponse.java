package uk.ac.aston.cs3mdd.whichdayapp.models;

import java.util.List;

//top level object
public class WeatherResponse {
  private String cod;
  private int message;
  private int cnt;
  private List<WeatherItem> list;
  private City city;

  public List<WeatherItem> getList() {
    return list;
  }

  public String getCod() {
    return cod;
  }

  public int getMessage() {
    return message;
  }

  public int getCnt() {
    return cnt;
  }


  public City getCity() {
    return city;
  }

}
