package models;

public class RideEntry {

  private int resortID;
  private String seasonID;
  private String dayID;
  private int skierID;

  private int time;
  private int liftID;

  public RideEntry(int resortID, String seasonID, String dayID, int skierID, int time,
      int liftID) {
    this.resortID = resortID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.skierID = skierID;
    this.time = time;
    this.liftID = liftID;
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

  public int getTime() {
    return time;
  }

  public int getLiftID() {
    return liftID;
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

  public void setTime(int time) {
    this.time = time;
  }

  public void setLiftID(int liftID) {
    this.liftID = liftID;
  }

  @Override
  public String toString() {
    return "RidePathPara{" +
        "resortID=" + resortID +
        ", seasonID='" + seasonID + '\'' +
        ", dayID='" + dayID + '\'' +
        ", skierID=" + skierID +
        ", time=" + time +
        ", liftID=" + liftID +
        '}';
  }
}
