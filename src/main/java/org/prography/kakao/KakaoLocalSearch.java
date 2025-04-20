package org.prography.kakao;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class KakaoLocalSearch {

    /*
    ?category_group_code=FD6&page=1&size=15&rect=126.920178530965%2C126.920178530965%2C126.920178530965%2C126.920178530965"
     */
    private final String BASE_URL = "https://dapi.kakao.com/v2/local/search/category.json?category_group_code=FD6&size=15";
    private final String API_KEY = "KakaoAK temp";
    private final HttpClient client = HttpClient.newHttpClient();

    public JsonObject callLocalSearchApi(String rect, int page) {
        String requestUrl = String.format("%s&rect=%s&page=%d", BASE_URL, rect, page);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(requestUrl))
            .header("Authorization", API_KEY)
            .header("Accept", "application/json")
            .GET()
            .build();

        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException();
            }
            return JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
