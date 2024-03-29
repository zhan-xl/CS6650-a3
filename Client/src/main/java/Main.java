import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
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
import services.PostRequestService;
import services.PostRequestService.PostRequestServiceBuilder;
import services.PrinterService;
import services.RideConsumer;
import services.RideProducer;
import services.WriterService;

public class Main {

  public static void main(String[] args) throws IOException, InterruptedException {
    String baseUrl = "http://18.236.186.127:8080/Server-1.0-XiaolinZhan/skiers";

    PostRequestService postService = new PostRequestServiceBuilder().setBaseUrl(baseUrl)
        .setNumOfRequests(24000).setRequestPerConsumer(1000).setNThreads(8).build();
    postService.makePostRequest();
    WriterService writerService = new WriterService(postService.getResponseList());
    writerService.writeFile();
    PrinterService printerService = new PrinterService(postService.getResponseList());
    printerService.print();
  }
}

