package org.prography.crawler.kakao.client;

import static io.github.resilience4j.ratelimiter.RateLimiter.waitForPermission;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import org.prography.config.RequestHeaders;
import org.prography.crawler.exception.KakaoMapRequestException;
import org.prography.crawler.kakao.dto.map.info.KakaoInfoResponse;
import org.prography.crawler.kakao.dto.map.review.KakaoReviewResponse;
import org.prography.parser.GsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KakaoMapClient {

    private static final Logger log = LoggerFactory.getLogger(KakaoMapClient.class);
    private static final String SCHEME = "https";
    private static final String HOST = "place-api.map.kakao.com";
    private static final String REVIEW_PATH = "/places/tab/reviews/kakaomap/";
    private static final String INFO_PATH = "/places/panel3/";

    private static final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    private final RateLimiter rateLimiter;

    public KakaoMapClient() {
        RateLimiterConfig config = RateLimiterConfig.custom()
            .limitForPeriod(5)
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .timeoutDuration(Duration.ofSeconds(5))
            .build();
        this.rateLimiter = RateLimiter.of("kakaoApiLimiter", config);
    }

    private void acquirePermitAndJitter() {
        try {
            // 최대 500ms 대기 후 permit 없으면 RequestNotPermitted 예외 발생
            waitForPermission(rateLimiter, 500);
        } catch (RequestNotPermitted ex) {
            log.warn("RateLimiter: permit 획득 타임아웃 (500ms), 바로 진행합니다.");
        }
        // 300~2000ms 사이 랜덤 지터
        long jitter = ThreadLocalRandom.current().nextLong(300, 2001);
        try {
            Thread.sleep(jitter);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    public KakaoInfoResponse callInfo(String placeId) {
        URI uri = buildInfoUri(placeId);

        String agent = RequestHeaders.USER_AGENTS.get(
            ThreadLocalRandom.current()
                .nextInt(RequestHeaders.USER_AGENTS.size())
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .header(RequestHeaders.ACCEPT, RequestHeaders.APPLICATION_JSON)
            .header(RequestHeaders.REFERER, "https://place.map.kakao.com/")
            .header(RequestHeaders.KAKAO_PF, RequestHeaders.WEB)
            .header(RequestHeaders.USER_AGENT, agent)
            .GET()
            .build();

        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            int code = response.statusCode();
            if (code != 200) {
                log.error("가게 정보 API 호출 실패: HTTP {} {}", code, response.body());
                throw new RuntimeException("API 호출 실패: HTTP " + code + "\n" + response.body());
            }
            return GsonProvider.GSON.fromJson(response.body(), KakaoInfoResponse.class);
        } catch (IOException | InterruptedException e) {
            log.error("API 호출 중 예외 발생 {}", e.getMessage(), e);
            Throwable cause = e;
            while (cause != null) {
                log.warn("예외 원인 체인: {}", cause.getClass().getName() + ": " + cause.getMessage());
                if (cause.getMessage() != null && cause.getMessage().contains("GOAWAY")) {
                    log.error("서버로부터 GOAWAY 프레임 수신됨 → 커넥션 종료로 판단");
                }
                cause = cause.getCause();
            }
            throw new KakaoMapRequestException("API 호출 중 예외 발생", e);
        }
    }

    private URI buildInfoUri(String placeId) {
        try {
            return new URI(SCHEME, HOST, INFO_PATH + placeId, null, null);
        } catch (URISyntaxException e) {
            log.error("잘못된 URI 파라미터: placeId={}", placeId, e);
            throw new IllegalArgumentException("잘못된 URI 파라미터 삽입");
        }
    }

    public KakaoReviewResponse callReview(String placeId, long lastReviewId) {
        acquirePermitAndJitter();

        URI uri = buildReviewUri(placeId, lastReviewId);

        String agent = RequestHeaders.USER_AGENTS.get(
            ThreadLocalRandom.current()
                .nextInt(RequestHeaders.USER_AGENTS.size())
        );

        // pf 값이 왜 필요할까 참 신기하네??
        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .header(RequestHeaders.ACCEPT, RequestHeaders.APPLICATION_JSON)
            .header(RequestHeaders.REFERER, "https://place.map.kakao.com/")
            .header(RequestHeaders.KAKAO_PF, RequestHeaders.WEB)
            .header(RequestHeaders.USER_AGENT, agent)
            .GET()
            .build();

        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            int code = response.statusCode();
            if (code != 200) {
                log.error("리뷰 API 호출 실패: HTTP {}, {}", code, response.body());
            }
            return GsonProvider.GSON.fromJson(response.body(), KakaoReviewResponse.class);
        } catch (IOException | InterruptedException e) {
            log.error("API 호출 중 예외 발생 {}", e.getMessage(), e);
            Throwable cause = e;
            while (cause != null) {
                log.warn("예외 원인 체인: {}", cause.getClass().getName() + ": " + cause.getMessage());
                if (cause.getMessage() != null && cause.getMessage().contains("GOAWAY")) {
                    log.error("서버로부터 GOAWAY 프레임 수신됨 → 커넥션 종료로 판단");
                }
                cause = cause.getCause();
            }

            throw new KakaoMapRequestException("API 호출 중 예외 발생", e);
        }
    }

    //order=RECOMMENDED&only_photo_review=false&previous_last_review_id=12618976
    private URI buildReviewUri(String placeId, long lastReviewId) {
        try {
            String query = String.format(
                "order=LATEST&only_photo_review=false&previous_last_review_id=%d"
                , lastReviewId);
            return new URI(SCHEME, HOST, REVIEW_PATH + placeId, query, null);
        } catch (URISyntaxException e) {
            log.error("잘못된 URI 파라미터: placeId={}", placeId, e);
            throw new IllegalArgumentException("잘못된 URI 파라미터 삽입");
        }
    }
}
