package com.xiaolinzhan.skidataserver.servlets;

import com.mongodb.client.MongoClient;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ListenerServlet implements ServletContextListener {
  private Connection connection;
  private MongoClient mongoClient;

  public void contextInitialized(ServletContextEvent event) {

  }

  public void contextDestroyed(ServletContextEvent event) {
    try {
      connection.close();
      mongoClient.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
