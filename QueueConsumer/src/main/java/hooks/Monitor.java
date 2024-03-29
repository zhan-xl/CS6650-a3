package hooks;

import java.util.concurrent.atomic.AtomicInteger;

public class Monitor implements Runnable{
  private AtomicInteger count;

  public Monitor(AtomicInteger count) {
    this.count = count;
  }

  @Override
  public void run() {
    int waitTime = 5000;
    while (true) {
      try {
        Thread.sleep(waitTime);
      } catch (InterruptedException e) {
        System.out.println("Shutdown...");
      }
      double freeMemory = Runtime.getRuntime().freeMemory() / Math.pow(10, 6);
      double usedMemory = Runtime.getRuntime().totalMemory() / Math.pow(10, 6) - freeMemory;
      int throughput = count.getAndSet(0) / (waitTime / 1000);
      System.out.println("Throughput:" + throughput + " per second; Memory Used: " + (int) usedMemory + "mb; Memory Available: " + (int) freeMemory + "mb.");
    }
  }



}
