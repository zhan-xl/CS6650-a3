package com.xiaolinzhan.skidataserver.models.liftRide;

public class LiftRidePath {

  private int resortID;
  private String seasonID;
  private String dayID;
  private int skierID;

  public LiftRidePath(int resortID, String seasonID, String dayID, int skierID) {
    this.resortID = resortID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.skierID = skierID;
  }

  public int getResortID() {
    return resortID;
  }

  public String getSeasonID() {
    return seasonID;
  }

  public String getDayID() {
    return dayID;
  }

  public int getSkierID() {
    return skierID;
  }

  public void setResortID(int resortID) {
    this.resortID = resortID;
  }

  public void setSeasonID(String seasonID) {
    this.seasonID = seasonID;
  }

  public void setDayID(String dayID) {
    this.dayID = dayID;
  }

  public void setSkierID(int skierID) {
    this.skierID = skierID;
  }

  @Override
  public String toString() {
    return "RideEntry{" +
        "resortID=" + resortID +
        ", seasonID='" + seasonID + '\'' +
        ", dayID='" + dayID + '\'' +
        ", skierID=" + skierID +
        '}';
  }
}
