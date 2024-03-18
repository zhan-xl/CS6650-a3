package com.xiaolinzhan.skidataserver.models.liftRide;

public class LiftRide {
  private LiftRidePath liftRidePath;
  private LiftRideBody liftRideBody;

  public LiftRide(LiftRidePath liftRidePath, LiftRideBody liftRideBody) {
    this.liftRidePath = liftRidePath;
    this.liftRideBody = liftRideBody;
  }

  public LiftRidePath getLiftRidePath() {
    return liftRidePath;
  }

  public LiftRideBody getLiftRideBody() {
    return liftRideBody;
  }

  public void setLiftRidePath(LiftRidePath liftRidePath) {
    this.liftRidePath = liftRidePath;
  }

  public void setLiftRideBody(LiftRideBody liftRideBody) {
    this.liftRideBody = liftRideBody;
  }
}
