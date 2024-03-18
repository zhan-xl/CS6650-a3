package services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import models.ResponseLog;
import models.RideEntry;

public class PostRequestService {
  private final String baseUrl;
  private final int numOfRequests;
  private final int numOfInitialThreads;
  private final int numOfMaxThreads;
  private final List<ResponseLog> responseList;

  public List<ResponseLog> getResponseList() {
    return responseList;
  }

  private PostRequestService(PostRequestServiceBuilder builder) {
    this.baseUrl = builder.baseUrl;
    this.numOfRequests = builder.numOfRequests;
    this.numOfInitialThreads = builder.numOfInitialThreads;
    this.numOfMaxThreads = builder.numOfMaxThreads;
    responseList = new CopyOnWriteArrayList<>();
  }


  public static class PostRequestServiceBuilder {
    private String baseUrl;
    private int numOfRequests;
    private final int numOfInitialThreads;
    private int numOfMaxThreads;

    public PostRequestServiceBuilder() {
      this.numOfInitialThreads = 32;
    }

    public PostRequestServiceBuilder setBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public PostRequestServiceBuilder setNumOfRequests(int numOfRequests) {
      this.numOfRequests = numOfRequests;
      return this;
    }

    public PostRequestServiceBuilder setNumOfMaxThreads(int numOfMaxThreads) {
      this.numOfMaxThreads = numOfMaxThreads;
      return this;
    }

    public PostRequestService build() {
      return new PostRequestService(this);
    }
  }

  public void makePostRequest() throws InterruptedException {
    // Creating a blocking queue to store random generated RideEntries.
    BlockingQueue<RideEntry> queue = new ArrayBlockingQueue<>(200);
    // Producer service to generate random RideEntries.
    ExecutorService producerService = new ThreadPoolExecutor(4, 8, 120, TimeUnit.SECONDS,
        new LinkedBlockingDeque<>());
    for (int i = 0; i < numOfRequests; i++) {
      producerService.execute(new RideProducer(queue));
    }
    // Start the timer for post requests.
    long start = System.currentTimeMillis();
    //Creating initial thread pool with 32 threads.
    List<Thread> threadsList = new ArrayList<>();
    for (int i = 0; i < numOfInitialThreads; i++) {
      Thread newThread = new Thread(new RideConsumer(baseUrl, queue, responseList));
      threadsList.add(newThread);
      newThread.start();
    }

    int remainingThreads = numOfRequests / RideConsumer.NUM_OF_ENTRY - numOfInitialThreads;
    // boolean to signal if any of the initial threads is finished.
    boolean isDone = false;
    while (remainingThreads > 0) {
      for (int i = 0; i < threadsList.size(); i++) {
        Thread thread = threadsList.get(i);
        if (thread == null) {
          continue;
        }
        // First thread finishes.
        if (!thread.isAlive() && !isDone) {
          isDone = true;
          System.out.println("One of the initial threads is completed.");
          // Creating more threads in the thread pool.
          for (int j = numOfInitialThreads; j < numOfMaxThreads; j++) {
            Thread newThread = new Thread(new RideConsumer(baseUrl, queue, responseList));
            threadsList.add(newThread);
            newThread.start();
            remainingThreads--;
          }
          Thread newThread = new Thread(new RideConsumer(baseUrl, queue, responseList));
          threadsList.set(i, newThread);
          newThread.start();
          remainingThreads--;
          // If any thread finishes, start a new one.
        } else if (!thread.isAlive()) {
          Thread newThread = new Thread(new RideConsumer(baseUrl, queue, responseList));
          threadsList.set(i, newThread);
          newThread.start();
          remainingThreads--;
        }
      }
    }
    // Wait for all threads complete.
    for (Thread thread : threadsList) {
      thread.join();
    }

    long end = System.currentTimeMillis();

    producerService.shutdown();

    try {
      if (!producerService.awaitTermination(120, TimeUnit.SECONDS)) {
        System.out.println("Fail to terminate the executor service.");
      }
    } catch (InterruptedException e) {
      System.out.println("Fail to wait for all tasks to finish.");
    }

    double totalTime = (end - start) / 1000.0;
    System.out.println("Total run time: " + totalTime + " second");
    System.out.printf("Total throughput: %.2f requests/second", numOfRequests / totalTime);
    System.out.println();
  }

}
