package pools;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import org.bson.Document;

public class MDBConnectionPool {

  private int capacity;
  private MongoClient mongoClient;
  private final BlockingQueue<MongoCollection> pool;
  public MDBConnectionPool(int maxSize) {
    this.capacity = maxSize;
    this.pool = new LinkedBlockingDeque<>(capacity);

    String connectionString =
        "mongodb+srv://" + System.getenv("MONGODB_USERNAME") + ":" + System.getenv(
            "MONGODB_PASSWORD")
            + "@xiaolinwebdev.sq1refr.mongodb.net/?retryWrites=true&w=majority&appName=XiaolinWebDev";

    ServerApi serverApi = ServerApi.builder()
        .version(ServerApiVersion.V1)
        .build();

    MongoClientSettings settings = MongoClientSettings.builder()
        .applyConnectionString(new ConnectionString(connectionString))
        .applyToConnectionPoolSettings(builder -> builder.minSize(64).maxSize(128))
        .serverApi(serverApi)
        .build();

    try {
      mongoClient = MongoClients.create(settings);
      for (int i = 0 ; i < capacity; i++) {
        MongoCollection<Document> collection = mongoClient.getDatabase("SkiDatabase").getCollection("liftRides");
        pool.put(collection);
      }
    } catch (MongoException | InterruptedException e) {
      System.out.println("Failed to create MongoDB client.");
    }
  }

  public MongoCollection borrowMDBCollection() {
    try {
      return pool.take();
    } catch (InterruptedException e) {
      throw new RuntimeException(("Error: no collection available. " + e));
    }
  }

  public void returnObject(MongoCollection mongoCollection) {
    if (mongoCollection != null) {
      pool.add(mongoCollection);
    }
  }

  public MongoClient getMongoClient() {
    return mongoClient;
  }

}
