package hooks;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import java.util.List;
import pools.RMQChannelPool;

public class ShutDownThread extends Thread{

  private MongoClient mongoClient;
  private RMQChannelPool rmqChannelPool;
  private List<Thread> threadList;
  private Thread monitorThread;

  public ShutDownThread(MessageConsumer mc) {
    this.mongoClient = mc.getMongoDBClient();
    this.rmqChannelPool = mc.getRmqChannelPool();
    this.threadList = mc.getThreadList();
    this.monitorThread = mc.getMonitorThread();
  }

  @Override
  public void run() {
    try {
      monitorThread.interrupt();
      mongoClient.close();
      rmqChannelPool.shutdown();
      for (Thread thread : threadList) {
        thread.interrupt();
      }
      System.out.println("Connections closed successfully.");
    } catch (MongoException e) {
      System.out.println("MongoDB client unable to close: " + e);
    } catch (Exception e) {
      System.out.println("Fail to close RabbitMq connection: " + e);
    }

  }
}
