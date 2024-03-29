package pools;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;

public class RMQChannelPool {

  private final BlockingQueue<Channel> pool;

  private final int capacity;

  private Connection connection;

  public RMQChannelPool(int maxSize, ConnectionFactory factory) {
    this.capacity = maxSize;
    this.pool = new LinkedBlockingDeque<>(capacity);
    try {
      connection = factory.newConnection();
      for (int i = 0; i < capacity; i++) {
        Channel channel;
        channel = connection.createChannel();
        pool.put(channel);
      }
    } catch (IOException | TimeoutException | InterruptedException e) {
      System.out.println("not able to create rmq channel. "+ e);
    }
  }

  public Channel borrowObject() throws IOException {
    try {
      return pool.take();
    } catch (InterruptedException e) {
      throw new RuntimeException(("Error: no channel available " + e));
    }
  }

  public void returnObject(Channel channel) {
    if (channel != null) {
      pool.add(channel);
    }
  }

  public int getCapacity() {
    return capacity;
  }

  public void shutdown() throws IOException, TimeoutException {
    for (Channel channel : pool) {
      channel.close();
    }
    connection.close();
  }
}

