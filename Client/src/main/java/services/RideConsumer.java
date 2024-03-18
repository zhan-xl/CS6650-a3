package services;

import apis.SkierApi;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import models.ResponseLog;
import models.RideEntry;

public class RideConsumer implements Runnable {

  private final SkierApi client;

  private final BlockingQueue<RideEntry> queue;
  private final List<ResponseLog> responseList;

  final public static int NUM_OF_ENTRY = 1000;
  final private int NUM_OF_RETRY = 5;

  public RideConsumer(String baseUrl, BlockingQueue<RideEntry> queue,
      List<ResponseLog> responseList) {
    client = new SkierApi(baseUrl);
    this.queue = queue;
    this.responseList = responseList;
  }

  @Override
  public void run() {
    for (int i = 0; i < NUM_OF_ENTRY; i++) {
      int statusCode = 0;
      long startTime = System.currentTimeMillis();
      try {
        RideEntry rideEntry = queue.take();
        for (int j = 0; j < NUM_OF_RETRY; j++) {
          statusCode = client.doPost(rideEntry);
          if (statusCode == 201) {
            break;
          }
        }
        long endTime = System.currentTimeMillis();
        ResponseLog responseLog = new ResponseLog(startTime, "POST", endTime - startTime,
            statusCode);
        responseList.add(responseLog);
      } catch (IOException | InterruptedException e) {
        ResponseLog responseLog = new ResponseLog(startTime, "POST", 0,
            statusCode);
        System.out.println(statusCode);
        responseList.add(responseLog);
      }
    }
  }
}
