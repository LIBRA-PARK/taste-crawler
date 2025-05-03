package org.prography.caller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import org.prography.caller.client.KakaoLocalSearchResponse;
import org.prography.config.KakaoConfig;
import org.prography.config.RequestHeaders;
import org.prography.parser.GsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KakaoLocalApiCaller {

    private static final Logger log = LoggerFactory.getLogger(KakaoLocalApiCaller.class);
    private static final String SCHEME = "https";
    private static final String HOST = "dapi.kakao.com";
    private static final String PATH = "/v2/local/search/category.json";
    private static final String CATEGORY = "FD6";
    private static final int SIZE = 15;

    private static final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    public KakaoLocalApiCaller() {
    }

    public KakaoLocalSearchResponse call(String rect, int page) throws KakaoLocalApiException {
        URI uri = buildUri(rect, page);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .header(RequestHeaders.AUTHORIZATION, KakaoConfig.KAKAO_API_KEY)
            .header(RequestHeaders.ACCEPT, RequestHeaders.APPLICATION_JSON)
            .GET()
            .build();

        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            int code = response.statusCode();
            if (code != 200) {
                log.error("API 호출 실패: HTTP {}", code);
                throw new RuntimeException("API 호출 실패: HTTP " + code + "\n" + response.body());
            }
            return GsonProvider.GSON.fromJson(response.body(), KakaoLocalSearchResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new KakaoLocalApiException("API 호출 중 예외 발생", e);
        }
    }

    private URI buildUri(String rect, int page) {
        try {
            String query = String.format(
                "category_group_code=%s&size=%d&rect=%s&page=%d",
                CATEGORY, SIZE, rect, page
            );
            return new URI(SCHEME, HOST, PATH, query, null);
        } catch (URISyntaxException e) {
            log.error("잘못된 URI 파라미터: rect={}, page={}", rect, page, e);
            throw new IllegalArgumentException("잘못된 URI 파라미터 삽입");
        }
    }

    public static class KakaoLocalApiException extends RuntimeException {

        public KakaoLocalApiException(String msg) {
            super(msg);
        }

        public KakaoLocalApiException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
