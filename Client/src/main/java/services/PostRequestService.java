package services;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import models.ResponseLog;
import models.RideEntry;

public class PostRequestService {
  private final String baseUrl;
  private final int numOfRequests;

  private final int requestPerConsumer;

  private final int nThreads;
  private final List<ResponseLog> responseList;

  public List<ResponseLog> getResponseList() {
    return responseList;
  }

  private PostRequestService(PostRequestServiceBuilder builder) {
    this.baseUrl = builder.baseUrl;
    this.numOfRequests = builder.numOfRequests;
    this.requestPerConsumer = builder.requestPerConsumer;
    this.nThreads = builder.nThreads;
    responseList = new CopyOnWriteArrayList<>();
  }


  public static class PostRequestServiceBuilder {
    private String baseUrl;
    private int numOfRequests;

    private int requestPerConsumer;

    private int nThreads;

    public PostRequestServiceBuilder setBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public PostRequestServiceBuilder setRequestPerConsumer(int requestPerConsumer) {
      this.requestPerConsumer = requestPerConsumer;
      return this;
    }

    public PostRequestServiceBuilder setNumOfRequests(int numOfRequests) {
      this.numOfRequests = numOfRequests;
      return this;
    }

    public PostRequestServiceBuilder setNThreads(int nThreads) {
      this.nThreads = nThreads;
      return this;
    }

    public PostRequestService build() {
      return new PostRequestService(this);
    }
  }

  public void makePostRequest() throws InterruptedException {
    // Creating a blocking queue to store random generated RideEntries.
    BlockingQueue<RideEntry> queue = new ArrayBlockingQueue<>(numOfRequests);
    RideProducer rp = new RideProducer(queue);
    for (int i = 0; i < numOfRequests; i++) {
      rp.run();
    }
    System.out.println("Ride entry generated.");


    // Start the timer for post requests.
    long start = System.currentTimeMillis();

    ExecutorService postRequestService = Executors.newFixedThreadPool(nThreads);
    for (int i = 0; i < numOfRequests / requestPerConsumer; i++) {
      postRequestService.execute(new RideConsumer(baseUrl, queue, responseList, requestPerConsumer));
    }

    postRequestService.shutdown();

    try {
      if (!postRequestService.awaitTermination(1200, TimeUnit.SECONDS)) {
        System.out.println("Fail to terminate the executor service.");
      }
    } catch (InterruptedException e) {
      System.out.println("Fail to wait for all tasks to finish.");
    }

    long end = System.currentTimeMillis();

    double totalTime = (end - start) / 1000.0;
    System.out.println("Total run time: " + totalTime + " second");
    System.out.printf("Total throughput: %.2f requests/second", numOfRequests / totalTime);
    System.out.println();
  }

}
