package org.prography.crawler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.NavigateOptions;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.WaitUntilState;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import org.prography.crawler.utils.ParserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaywrightManager {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightManager.class);
    private final String KAKAO_INFO_PREFIX = "/places/panel3/";
    private final String KAKAO_REVIEW_PREFIX = "/places/tab/reviews/kakaomap/";
    private final String NAVER_REVIEW_URL = "https://map.naver.com/p/smart-around/place/%s?placePath=/review";
    private final String NAVER_API_PREFIX = "https://map.naver.com/p/api/search/allSearch";
    private final String NAVER_INFO_URL = "https://map.naver.com/p/search/";

    private final Playwright playwright;
    private final Browser browser;

    private static class Holder {

        private static final PlaywrightManager INSTANCE = new PlaywrightManager();
    }

    public static PlaywrightManager instance() {
        return Holder.INSTANCE;
    }

    private PlaywrightManager() {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
            .setHeadless(false)
            .setTimeout(30_000)
            .setArgs(List.of(
                "--disable-blink-features=AutomationControlled",
                "--no-sandbox",
                "--disable-setuid-sandbox"
            ))
        );
        log.info("Playwright & Browser initialized");
    }


    /**
     * 주어진 placeUrl/#comment 페이지로 이동해, /places/tab/reviews/kakaomap/{placeId} 응답을 가로채서 반환합니다.
     *
     * @param placeUrls 카카오맵 place 페이지 URL 리스트
     * @return 리뷰 API 응답 JSON
     */
    public Map<String, JsonObject> fetchKaKaoReviews(List<String> placeUrls) {
        Map<String, JsonObject> result = new HashMap<>();

        for (String url : placeUrls) {
            String placeId = ParserUtils.extractKakaoPlaceId(url);

            try (
                BrowserContext browserContext = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)…")
                    .setLocale("ko-KR")
                );

                Page page = browserContext.newPage();
            ) {
                AtomicReference<JsonObject> infoResponse = new AtomicReference<>();
                AtomicReference<JsonObject> reviewResponse = new AtomicReference<>();

                page.onResponse(response -> {
                    if (!response.ok()) {
                        return;
                    }
                    if (!response.request().resourceType().matches("xhr|fetch")) {
                        return;
                    }

                    try {
                        String body = response.text();
                        String responseUrl = response.url();
                        if (responseUrl.contains(KAKAO_INFO_PREFIX)) {
                            infoResponse.set(JsonParser.parseString(body).getAsJsonObject());
                            log.debug("Captured panel JSON for {}", placeId);
                        } else if (responseUrl.contains(KAKAO_REVIEW_PREFIX)) {
                            reviewResponse.set(JsonParser.parseString(body).getAsJsonObject());
                            log.debug("Captured reviews JSON for {}", placeId);
                        }
                    } catch (PlaywrightException | JsonSyntaxException e) {
                        log.warn("Error parsing response for {}: {}", placeId, e.getMessage());
                    }
                });

                page.navigate(url + "#comment", new NavigateOptions()
                    .setTimeout(30_000L)
                    .setWaitUntil(WaitUntilState.NETWORKIDLE)
                );

                if (infoResponse.get() != null && reviewResponse.get() != null) {
                    JsonObject merged = new JsonObject();
                    merged.add("info", infoResponse.get());
                    merged.add("reviews", reviewResponse.get());
                    result.put(placeId, merged);
                    log.info("Merged JSON for {}", placeId);
                } else {
                    log.warn("Timeout waiting for both responses for {}", placeId);
                }

            } catch (PlaywrightException e) {
                log.warn("Playwright error for {}: {}", placeId, e.getMessage());
            } catch (JsonSyntaxException e) {
                log.warn("JSON parse error for {}: {}", placeId, e.getMessage());
            }
        }
        return result;
    }

    public Map<String, JsonObject> fetchNaverBusinessId(List<String> ids) {
        Map<String, JsonObject> result = new HashMap<>();

        for (String id : ids) {
            try (
                BrowserContext browserContext = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)…")
                    .setLocale("ko-KR")
                );

                Page page = browserContext.newPage();
            ) {
                Response resp = page.waitForResponse(
                    r -> r.ok() && r.request().resourceType().matches("xhr|fetch") && r.url()
                        .startsWith(NAVER_API_PREFIX),
                    () -> page.navigate(NAVER_INFO_URL + id, new NavigateOptions()
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                        .setTimeout(30_000L))
                );

                JsonObject root = JsonParser.parseString(resp.text()).getAsJsonObject();
                JsonArray list;
                try {
                    list = root
                        .getAsJsonObject("result")
                        .getAsJsonObject("place")
                        .getAsJsonArray("list");

                    if (!list.isEmpty()) {
                        result.put(id, list.get(0).getAsJsonObject());
                    } else {
                        log.warn("NAVER ID FAIL {}", id);
                    }
                } catch (Exception e) {
                    // Naver에 해당 주소가 없는 경우
                    log.warn("NAVER ID FAIL {}", id);
                }
            } catch (PlaywrightException e) {
                System.err.println("Playwright error: " + e.getMessage());
            } catch (JsonSyntaxException e) {
                System.err.println("JSON parse error: " + e.getMessage());
            }
        }

        return result;
    }

    public Map<String, JsonObject> fetchNaverReview(Map<String, String> ids) {
        Map<String, JsonObject> result = new HashMap<>();

        for (Entry<String, String> entry : ids.entrySet()) {
            try (
                BrowserContext browserContext = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)…")
                    .setLocale("ko-KR")
                );

                Page page = browserContext.newPage();
            ) {
                Response resp = page.waitForResponse(
                    response -> {
                        if (!response.url().contains("/graphql")) {
                            return false;
                        }
                        String post = response.request().postData();
                        if (post == null || !post.contains("visitorReviews")) {
                            return false;
                        }
                        String bizIds = response.headers().get("x-gql-businessids");
                        return entry.getValue().equals(bizIds);
                    },
                    () -> {
                        page.navigate(
                            String.format(
                                "https://map.naver.com/p/smart-around/place/%s?placePath=/review",
                                entry.getValue()
                            ));
                    }
                );
                result.put(entry.getKey(), JsonParser.parseString(resp.text()).getAsJsonObject());
            }
        }
        return result;
    }

    public void close() {
        log.info("Closing Playwright & Browser");
        browser.close();
        playwright.close();
    }
}
