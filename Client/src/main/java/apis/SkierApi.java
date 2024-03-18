package apis;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import models.RideEntry;

public class SkierApi {

  private final String url;
  private final HttpClient httpClient = HttpClient.newBuilder().version(Version.HTTP_1_1)
      .connectTimeout(Duration.ofSeconds(4)).build();

  public SkierApi(String url) {
    this.url = url;
  }

  public void doGet(RideEntry rideEntry) throws IOException, InterruptedException {
    String getUrl = url + "/" + rideEntry.getResortID() + "/seasons/"
        + rideEntry.getSeasonID() + "days/" + rideEntry.getDayID()
        + "/skiers/" + rideEntry.getSkierID();
    HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(getUrl)).build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    System.out.println(response.statusCode());
  }

  public int doPost(RideEntry rideEntry)
      throws IOException, InterruptedException {
    String postUrl = url + "/" + rideEntry.getResortID() + "/seasons/"
        + rideEntry.getSeasonID() + "/days/" + rideEntry.getDayID()
        + "/skiers/" + rideEntry.getSkierID();
    String requestBody =
        "{\"time\": \"" + rideEntry.getTime() + "\", \"liftID\": \"" + rideEntry.getLiftID()
            + "\"}";
    HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(
            requestBody))
        .uri(URI.create(postUrl)).header("Content-Type", "application/json")
        .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    return response.statusCode();
  }
}
