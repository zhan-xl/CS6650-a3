package services;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import models.RideEntry;

public class RideProducer implements Runnable {

  private BlockingQueue<RideEntry> queue;
  public RideProducer(BlockingQueue<RideEntry> queue) {
    this.queue = queue;
  }

  @Override
  public void run() {
    RideEntry rideEntry = this.getRandRide();
    try {
      queue.put(rideEntry);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private RideEntry getRandRide() {
    Random rand = new Random();
    int resortID = rand.nextInt(10) + 1;
    String seasonID = "2024";
    String dayID = "1";
    int skierID = rand.nextInt(100000) + 1;
    int time = rand.nextInt(360) + 1;
    int liftID = rand.nextInt(40) + 1;
    return new RideEntry(resortID, seasonID, dayID, skierID, time, liftID);
  }

}
