package org.prography.caller.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import org.prography.config.KakaoConfig;
import org.prography.config.RequestHeaders;

public class KakaoLocalApiClient {

    private final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    public JsonObject callLocalSearchApi(String rect, int page) {
        String requestUrl = String.format("%s?category_group_code=%s&size=%d&rect=%s&page=%d",
            KakaoConfig.BASE_URL,
            KakaoConfig.CATEGORY_GROUP,
            KakaoConfig.DEFAULT_SIZE,
            URI.create(rect).toASCIIString(),
            page
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(requestUrl))
            .header(RequestHeaders.AUTHORIZATION, KakaoConfig.API_KEY)
            .header(RequestHeaders.ACCEPT, RequestHeaders.APPLICATION_JSON)
            .GET()
            .build();

        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            int code = response.statusCode();
            if (code != 200) {
                throw new ApiException("API 호출 실패: HTTP " + code + "\n" + response.body());
            }
            return JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("API 호출 중 예외 발생", e);
        }
    }

    public static class ApiException extends RuntimeException {

        public ApiException(String msg) {
            super(msg);
        }

        public ApiException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
