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

  private int requestPerConsumer;
  final private int NUM_OF_RETRY = 5;

  private int statusCode;

  public RideConsumer(String baseUrl, BlockingQueue<RideEntry> queue,
      List<ResponseLog> responseList, int requestPerConsumer) {
    client = new SkierApi(baseUrl);
    this.queue = queue;
    this.requestPerConsumer = requestPerConsumer;
    this.responseList = responseList;
  }

  @Override
  public void run() {
    for (int i = 0; i < requestPerConsumer; i++) {
      statusCode = 0;
      int waitSecond = 0;
      long startTime = System.currentTimeMillis();
      try {
        RideEntry rideEntry = queue.take();
        for (int j = 0; j < NUM_OF_RETRY; j++) {
          Thread.sleep(waitSecond);
          statusCode = client.doPost(rideEntry);
          if (statusCode == 201) {
            break;
          } else {
            waitSecond = 1000 * (int) Math.pow(4, j);
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
