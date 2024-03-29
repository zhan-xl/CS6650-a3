import hooks.MessageConsumer;
import hooks.ShutDownThread;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Main {

  public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
    System.setProperty("http.proxyHost", "127.0.0.1");
    System.setProperty("http.proxyPort", "2006");

    System.setProperty("https.proxyHost", "127.0.0.1");
    System.setProperty("https.proxyPort","2006");
    MessageConsumer mc = new MessageConsumer(System.getenv("RMQ_URL"), 5672, System.getenv("RMQ_USERNAME"),
        System.getenv("RMQ_PASSWORD"), "vh");
    mc.connectToRabbitMq();
    mc.connectToMongoDB();
    mc.startConsume();

    Runtime.getRuntime().addShutdownHook(new ShutDownThread(mc));
  }
}
