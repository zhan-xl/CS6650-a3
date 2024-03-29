package com.xiaolinzhan.skidataserver.models.liftRide;

import java.util.Objects;

public class LiftRideDocument {

  private int time;
  private int liftID;
  private int resortID;
  private String seasonID;
  private String dayID;
  private int skierID;

  @Override
  public String toString() {
    return "LiftRideDocument{" +
        "time=" + time +
        ", liftID=" + liftID +
        ", resortID=" + resortID +
        ", seasonID='" + seasonID + '\'' +
        ", dayID='" + dayID + '\'' +
        ", skierID=" + skierID +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LiftRideDocument that = (LiftRideDocument) o;
    return time == that.time && liftID == that.liftID && resortID == that.resortID
        && skierID == that.skierID && Objects.equals(seasonID, that.seasonID)
        && Objects.equals(dayID, that.dayID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(time, liftID, resortID, seasonID, dayID, skierID);
  }

  public void setTime(int time) {
    this.time = time;
  }

  public void setLiftID(int liftID) {
    this.liftID = liftID;
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

  public int getTime() {
    return time;
  }

  public int getLiftID() {
    return liftID;
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

  public LiftRideDocument(LiftRidePath liftRidePath, LiftRideBody liftRideBody) {
    this.time = liftRideBody.getTime();
    this.liftID = liftRideBody.getLiftID();
    this.resortID = liftRidePath.getResortID();
    this.seasonID = liftRidePath.getSeasonID();
    this.dayID = liftRidePath.getDayID();
    this.skierID = liftRidePath.getSkierID();
  }
}

