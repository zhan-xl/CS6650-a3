package services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import models.ResponseLog;

public class PrinterService {
  private final List<ResponseLog> responseList;

  public PrinterService(List<ResponseLog> responseList) {
    this.responseList = responseList;
  }

  public void print() {
    System.out.println("Number of successes POST request: " + getNumOfSuccess());
    System.out.println("Number of failure POST request: " + getNumOfFailure());
    System.out.println("Mean response time: " + getMeanResTime() + " ms");
    System.out.println("Median response time: " + getMedianResTime() + " ms");
    System.out.println("Minimum response time: " + getMinResTime() + " ms");
    System.out.println("Maximum response time: " + getMaxResTime() + " ms");
    System.out.println("99th percentile response time: " + getPercentile(99.0) + " ms");
  }

  private int getPercentile(double percentile) {
    List<Integer> responseTime = sortResponseTime();
    int index = (int) Math.ceil(percentile / 100.0 * responseTime.size());
    return responseTime.get(index - 1);
  }

  private int getMeanResTime() {
    int sum = 0;
    for (ResponseLog responseLog : responseList) {
      sum += (int) responseLog.getLatency();
    }
    return sum / responseList.size();
  }

  private List<Integer> sortResponseTime() {
    List<Integer> responseTime = new ArrayList<>(responseList.size());
    for (ResponseLog responseLog : responseList) {
      responseTime.add((int) responseLog.getLatency());
    }
    Collections.sort(responseTime);
    return responseTime;
  }

  private double getMedianResTime() {
    List<Integer> responseTime = sortResponseTime();
    int middleIndex = responseTime.size() / 2;
    if (responseTime.size() % 2 == 0) {
      return (responseTime.get(middleIndex - 1) + responseTime.get(middleIndex)) / 2.0;
    } else {
      return responseTime.get(middleIndex);
    }
  }

  private int getMinResTime() {
    return sortResponseTime().get(0);
  }

  private int getMaxResTime() {
    return sortResponseTime().get(sortResponseTime().size() - 1);
  }

  private int getNumOfSuccess() {
    int numOfSuccess = 0;
    for (ResponseLog responseLog : responseList) {
      if (responseLog.getStatusCode() == 201) {
        numOfSuccess += 1;
      }
    }
    return numOfSuccess;
  }

  private int getNumOfFailure() {
    int numOfFailure = 0;
    for (ResponseLog responseLog : responseList) {
      if (responseLog.getStatusCode() != 201) {
        numOfFailure += 1;
      }
    }
    return numOfFailure;
  }
}
