package services;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import models.ResponseLog;

public class WriterService {
  private final List<ResponseLog> responseList;

  private final String filePath;

  public WriterService(List<ResponseLog> responseList) {
    this.responseList = responseList;
    this.filePath = "/Users/xiaolinzhan/Documents/CS6650/CS6650-assignment1/Client/log/responseLog.csv";
  }

  public WriterService(List<ResponseLog> responseList, String filePath) {
    this.responseList = responseList;
    this.filePath = filePath;
  }

  public void writeFile()
      throws IOException {
    FileWriter writer = new FileWriter(filePath);
    String firstLine = "start time, request type, latency, status code,";
    writer.write(firstLine);
    writer.write("\n");
    for (ResponseLog responseLog : responseList) {
      String line = responseLog.getStartTime() + ", " + responseLog.getRequestType() + ", "
          + responseLog.getLatency() + ", " + responseLog.getStatusCode() + ",";
      writer.write(line);
      writer.write("\n");
    }
    writer.close();
  }

}
