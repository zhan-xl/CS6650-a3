import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class Main {

  private final static String QUEUE_NAME = "liftRides";

  public static void main(String[] args) throws IOException, TimeoutException {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(System.getenv("RABBITMQ_URL"));
    factory.setPort(5672);
    factory.setUsername("SkiServer");
    factory.setPassword("84e505a1-868e-470d-ac58-9d4431863248");
    factory.setVirtualHost("vh");

    ExecutorService es = Executors.newSingleThreadExecutor();

    Connection connection = factory.newConnection(es);
    Channel channel = connection.createChannel();
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

//      String message = "Hello World!";
//      channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
//      System.out.println(" [x] Sent '" + message + "'");
    System.out.println("Connected to rabbitmq server at: " + System.getenv("RABBITMQ_URL"));

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
    };
    channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
    });

  }
}
