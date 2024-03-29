package hooks;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import pools.MDBConnectionPool;
import pools.RMQChannelPool;

public class MessageConsumer {

  private final String QUEUE_NAME = "liftRides";
  private final ConnectionFactory factory;
  private RMQChannelPool rmqChannelPool;
  private pools.MDBConnectionPool mbdConnectionPool;

  private List<Thread> threadList;
  private Thread monitorThread;

  public MessageConsumer(String rabbitMqUrl, int port, String username, String password,
      String virtualHost) {
    factory = new ConnectionFactory();
    factory.setHost(rabbitMqUrl);
    factory.setPort(port);
    factory.setUsername(username);
    factory.setPassword(password);
    factory.setVirtualHost(virtualHost);
  }

  public void connectToRabbitMq() {
    rmqChannelPool = new RMQChannelPool(Integer.parseInt(System.getenv("NUM_OF_CONSUMERS")), factory);
    System.out.println("Connected to rabbitmq server at: " + System.getenv("RMQ_URL"));
  }

  public void connectToMongoDB() {
    mbdConnectionPool = new MDBConnectionPool(Integer.parseInt(System.getenv("NUM_OF_CONSUMERS")));
  }

  public void startConsume() throws InterruptedException {
    AtomicInteger count = new AtomicInteger();
    Runnable consumer = () -> {
      try {
        Channel channel = rmqChannelPool.borrowObject();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        MongoCollection collection = mbdConnectionPool.borrowMDBCollection();
        DeliverCallback deliverCallback = new DatabaseDeliverCallback(collection, count);
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
        });
        rmqChannelPool.returnObject(channel);
        mbdConnectionPool.returnObject(collection);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };
    threadList = new ArrayList<>();
    for (int i = 0; i < Integer.parseInt(System.getenv("NUM_OF_CONSUMERS")); i++) {
      Thread newThread = new Thread(consumer);
      threadList.add(newThread);
      newThread.start();
    }
    monitorThread = new Thread(new Monitor(count));
    monitorThread.start();
  }

  public MongoClient getMongoDBClient() {
    return mbdConnectionPool.getMongoClient();
  }

  public RMQChannelPool getRmqChannelPool() {
    return rmqChannelPool;
  }

  public List<Thread> getThreadList() {
    return threadList;
  }

  public Thread getMonitorThread() {
    return monitorThread;
  }
}
