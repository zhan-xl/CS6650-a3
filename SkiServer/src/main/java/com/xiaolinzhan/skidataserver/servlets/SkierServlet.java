package com.xiaolinzhan.skidataserver.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.xiaolinzhan.skidataserver.models.liftRide.LiftRide;
import com.xiaolinzhan.skidataserver.models.liftRide.LiftRideBody;
import com.xiaolinzhan.skidataserver.models.liftRide.LiftRidePath;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

@WebServlet(name = "skier servlet", value = "/skiers/*")
public class SkierServlet extends HttpServlet {

  private JsonObject responseJson = new JsonObject();

  private JsonObject liftRidePathJson;
  private Gson gson = new Gson();
  private String message;
  private ConnectionFactory factory;
  private String QUEUE_NAME = "liftRides";

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    factory = new ConnectionFactory();
    factory.setHost(System.getenv("RABBITMQ_URL"));
    factory.setPort(5672);
    factory.setUsername("SkiServer");
    factory.setPassword("84e505a1-868e-470d-ac58-9d4431863248");
    factory.setVirtualHost("vh");
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String urlPath = request.getPathInfo();
    String body = getBody(request);
    LiftRideBody liftRideBody = gson.fromJson(body, LiftRideBody.class);
    LiftRidePath liftRidePath = parseUrlPath(urlPath);

    if (liftRidePath == null || !dataValidation(liftRidePath, liftRideBody)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      message = "invalid request.";
      responseJson.addProperty("message", message);
      response.getWriter().write(responseJson.toString());
      return;
    }

    if (sendMessage(new LiftRide(liftRidePath, liftRideBody))) {
      response.setStatus(HttpServletResponse.SC_CREATED);
    } else {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      message = "rabbit server url: " + System.getenv("RABBITMQ_URL");
      responseJson.addProperty("message", message);
      response.getWriter().write(responseJson.toString());
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    message = "you have waited for 1 sec";
    responseJson.addProperty("message", message);
    response.getWriter().write(responseJson.toString());

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
    } else
      return liftRideBody.getLiftID() >= 1 && liftRideBody.getLiftID() <= 40;
  }

  private boolean sendMessage(LiftRide liftRide) {
    try (Connection connection = factory.newConnection();
        Channel channel = connection.createChannel()) {
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);
      channel.basicPublish("", QUEUE_NAME, null, gson.toJson(liftRide, LiftRide.class).getBytes());
      return true;
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return false;
    }
  }
}