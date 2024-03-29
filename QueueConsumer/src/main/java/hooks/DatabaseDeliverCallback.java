package hooks;

import com.google.gson.Gson;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import models.LiftRide;
import org.bson.Document;
import org.bson.types.ObjectId;

public class DatabaseDeliverCallback implements DeliverCallback {

  private final MongoCollection mongoCollection;

  private final AtomicInteger count;

  public DatabaseDeliverCallback(MongoCollection mongoCollection, AtomicInteger count) {
    this.mongoCollection = mongoCollection;
    this.count = count;
  }

  @Override
  public void handle(String s, Delivery delivery) {
    Gson gson = new Gson();
    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
    LiftRide liftRide = gson.fromJson(message, LiftRide.class);
    try {
      Document document = new Document()
          .append("_id", new ObjectId())
          .append("dayID", liftRide.getDayID())
          .append("skierID", liftRide.getSkierID())
          .append("time", liftRide.getTime())
          .append("liftID", liftRide.getLiftID())
          .append("resortID", liftRide.getResortID())
          .append("seasonID", liftRide.getSeasonID());
      InsertOneResult result = mongoCollection.insertOne(document);
      count.incrementAndGet();
    } catch (MongoException e) {
      System.out.println("Unable to insert due to an error: " + e);
    }
  }
}
