package com.xiaolinzhan.skidataserver.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.xiaolinzhan.skidataserver.models.liftRide.LiftRideDocument;
import com.xiaolinzhan.skidataserver.models.liftRide.LiftRideBody;
import com.xiaolinzhan.skidataserver.models.liftRide.LiftRidePath;
import com.xiaolinzhan.skidataserver.rmqpool.RMQChannelPool;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

import com.rabbitmq.client.ConnectionFactory;
import org.bson.Document;
import org.bson.conversions.Bson;

@WebServlet(name = "skier servlet", value = "/skiers/*")
public class SkierServlet extends HttpServlet {

  private RMQChannelPool channelPool;

  private MongoCollection<Document> mongoCollection;

  private int lowerBound;

  private int upperBound;

  private int queueSize;


  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(System.getenv("RMQ_URL"));
    factory.setPort(5672);
    factory.setUsername(System.getenv("RMQ_USERNAME"));
    factory.setPassword(System.getenv("RMQ_PASSWORD"));
    factory.setVirtualHost("vh");

    int maxThreads = Integer.parseInt(System.getenv("MAX_THREADS"));
    lowerBound = Integer.parseInt(System.getenv("LOWER_BOUND"));
    upperBound = Integer.parseInt(System.getenv("UPPER_BOUND"));
    queueSize = 0;

    channelPool = new RMQChannelPool(maxThreads, factory);

    String connectionString =
        "mongodb+srv://" + System.getenv("MONGODB_USERNAME") + ":" + System.getenv(
            "MONGODB_PASSWORD")
            + "@xiaolinwebdev.sq1refr.mongodb.net/?retryWrites=true&w=majority&appName=XiaolinWebDev";

    ServerApi serverApi = ServerApi.builder()
        .version(ServerApiVersion.V1)
        .build();

    MongoClientSettings settings = MongoClientSettings.builder()
        .applyConnectionString(new ConnectionString(connectionString))
        .serverApi(serverApi)
        .build();

    try {
      MongoClient mongoClient = MongoClients.create(settings);
      MongoDatabase database = mongoClient.getDatabase("SkiDatabase");
      mongoCollection = database.getCollection("liftRides");
    } catch (MongoException e) {
      System.out.println("Failed to create MongoDB client.");
    }
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String urlPath = request.getPathInfo();
    String body = getBody(request);
    LiftRideBody liftRideBody = new Gson().fromJson(body, LiftRideBody.class);
    LiftRidePath liftRidePath = parseUrlPath(urlPath);

    if (liftRidePath == null || !dataValidation(liftRidePath, liftRideBody)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      JsonObject responseJson = new JsonObject();
      responseJson.addProperty("message", "invalid request.");
      response.getWriter().write(responseJson.toString());
      return;
    }

    LiftRideDocument liftRideDocument = new LiftRideDocument(liftRidePath, liftRideBody);

    try {
      Channel channel = channelPool.borrowObject();
      AMQP.Queue.DeclareOk dok = channel.queueDeclare("liftRides", false, false, false, null);
      queueSize = dok.getMessageCount();
      double p = 1 - (double) (queueSize - lowerBound) / (upperBound - lowerBound);
      boolean doPost = getBoolean(p);
      if (doPost) {
        channel.basicPublish("", "liftRides", null,
            new Gson().toJson(liftRideDocument, LiftRideDocument.class).getBytes());
        response.setStatus(HttpServletResponse.SC_CREATED);
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("Lift Ride Inserted", liftRideDocument.toString());
        response.getWriter().write(responseJson.toString());
      } else {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("message", "server busy");
        response.getWriter().write(responseJson.toString());
      }
      channelPool.returnObject(channel);
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      JsonObject responseJson = new JsonObject();
      responseJson.addProperty("Error", e.toString());
      response.getWriter().write(responseJson.toString());
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    String action = request.getParameter("action");
    String skierID = request.getParameter("skierID");
    String seasonID = request.getParameter("seasonID");
    String dayID = request.getParameter("dayID");
    String resortID = request.getParameter("resortID");
    JsonObject responseJson = new JsonObject();
    switch (action) {
      case "get-days":
        if (skierID == null || seasonID == null) {
          responseJson.addProperty("message", "bad request, missing parameters.");
          response.getWriter().write(responseJson.toString());
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        List<String> days = getDays(skierID, seasonID);
        responseJson.addProperty("days", days.toString());
        response.getWriter().write(responseJson.toString());
        break;
      case "get-vertical":
        if (skierID == null || seasonID == null || dayID == null) {
          responseJson.addProperty("message", "bad request, missing parameters.");
          response.getWriter().write(responseJson.toString());
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        int vertical = getVertical(skierID, seasonID, dayID);
        responseJson.addProperty("vertical", vertical);
        response.getWriter().write(responseJson.toString());
        break;
      case "get-lifts":
        if (skierID == null || seasonID == null || dayID == null) {
          responseJson.addProperty("message", "bad request, missing parameters.");
          response.getWriter().write(responseJson.toString());
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        List<Integer> lifts = getLifts(skierID, seasonID, dayID);
        responseJson.addProperty("lifts", lifts.toString());
        response.getWriter().write(responseJson.toString());
        break;
      case "get-skiers":
        if (resortID == null || seasonID == null || dayID == null) {
          responseJson.addProperty("message", "bad request, missing parameters.");
          response.getWriter().write(responseJson.toString());
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        List<Integer> skiers = getNumberOfSkiers(resortID, seasonID, dayID);
        responseJson.addProperty("skiers", skiers.toString());
        response.getWriter().write(responseJson.toString());
        break;
      case "health-check":
        response.setStatus(HttpServletResponse.SC_OK);
    }
  }

  private LiftRidePath parseUrlPath(String urlPath) {
    if (urlPath == null || urlPath.isEmpty()) {
      return null;
    }
    String[] urlParas = urlPath.split("/");
    if (urlParas.length < 8 || !urlParas[0].isEmpty() || !urlParas[2].equals("seasons")
        || !urlParas[4].equals("days") || !urlParas[6].equals("skiers")) {
      return null;
    }
    String resortID = urlParas[1];
    String seaSonsID = urlParas[3];
    String dayID = urlParas[5];
    String skierID = urlParas[7];

    return new LiftRidePath(Integer.parseInt(resortID), seaSonsID, dayID,
        Integer.parseInt(skierID));
  }

  public String getBody(HttpServletRequest request) throws IOException {
    BufferedReader reader = request.getReader();
    StringBuilder stringBuilder = new StringBuilder();

    String line;
    while ((line = reader.readLine()) != null) {
      stringBuilder.append(line);
      stringBuilder.append(System.lineSeparator());
    }

    return stringBuilder.toString();
  }

  private boolean dataValidation(LiftRidePath liftRidePath, LiftRideBody liftRideBody) {
    if (liftRidePath.getResortID() < 1
        || liftRidePath.getResortID() > 10) {
      return false;
    } else if (!liftRidePath.getSeasonID().equals("2024")) {
      return false;
    } else if (!liftRidePath.getDayID().equals("1")) {
      return false;
    } else if (liftRidePath.getSkierID() < 1
        || liftRidePath.getSkierID() > 100000) {
      return false;
    } else if (liftRideBody.getTime() < 1 || liftRideBody.getTime() > 360) {
      return false;
    } else {
      return liftRideBody.getLiftID() >= 1 && liftRideBody.getLiftID() <= 40;
    }
  }

  private List<String> getDays(String skierID, String seasonID) {
    Bson projectionFields = Projections.fields(Projections.include("dayID"),
        Projections.excludeId());
    Bson filter = Filters.and(Filters.eq("skierID", Integer.parseInt(skierID)),
        Filters.eq("seasonID", seasonID));
    MongoCursor<Document> cursor = mongoCollection.find(filter).projection(projectionFields).iterator();
    List<String> days = new ArrayList<>();
    while (cursor.hasNext()) {
      days.add(cursor.next().getString("dayID"));
    }
    return days;
  }

  private int getVertical(String skierID, String seasonID, String dayID) {
    Bson projectionFields = Projections.fields(Projections.include("liftID"),
        Projections.excludeId());
    Bson filter = Filters.and(Filters.eq("skierID", Integer.parseInt(skierID)),
        Filters.eq("seasonID", seasonID), Filters.eq("dayID", dayID));
    MongoCursor<Document> cursor = mongoCollection.find(filter).projection(projectionFields)
        .iterator();
    int totalVertical = 0;
    while (cursor.hasNext()) {
      totalVertical += cursor.next().getInteger("liftID") * 10;
    }
    return totalVertical;
  }

  private List<Integer> getLifts(String skierID, String seasonID, String dayID) {
    Bson projectionFields = Projections.fields(Projections.include("liftID"),
        Projections.excludeId());
    Bson filter = Filters.and(Filters.eq("skierID", Integer.parseInt(skierID)),
        Filters.eq("seasonID", seasonID), Filters.eq("dayID", dayID));
    MongoCursor<Document> cursor = mongoCollection.find(filter).projection(projectionFields)
        .iterator();
    List<Integer> lifts = new ArrayList<>();
    while (cursor.hasNext()) {
      lifts.add(cursor.next().getInteger("liftID"));
    }
    return lifts;
  }

  private List<Integer> getNumberOfSkiers(String resortID, String seasonID, String dayID) {
    Bson projectionFields = Projections.fields(Projections.include("skierID"),
        Projections.excludeId());
    Bson filter = Filters.and(Filters.eq("resortID", Integer.parseInt(resortID)),
        Filters.eq("seasonID", seasonID), Filters.eq("dayID", dayID));
    MongoCursor<Document> cursor = mongoCollection.find(filter).projection(projectionFields).iterator();
    List<Integer> skiers = new ArrayList<>();
    while (cursor.hasNext()) {
      skiers.add(cursor.next().getInteger("skierID"));
    }
    return skiers;
  }

  private boolean getBoolean(double p) {
    Random rand = new Random();
    return rand.nextFloat() < p;
  }
}