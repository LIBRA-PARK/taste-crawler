package org.prography.kakao.crawler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.NavigateOptions;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaywrightManager {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightManager.class);

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
            .setHeadless(true)
            .setTimeout(30_000)
        );
        log.info("Playwright & Browser initialized");
    }

    /**
     * 주어진 placeUrl/#comment 페이지로 이동해, /places/tab/reviews/kakaomap/{placeId} 응답을 가로채서 반환합니다.
     *
     * @param placeUrl 카카오맵 place 페이지 URL
     * @return 리뷰 API 응답 JSON
     */
    public JsonObject fetchKaKaoReviews(String placeUrl) {
        String placeId = extractPlaceId(placeUrl);
        String panelFrag = "/places/panel3/" + placeId;
        String reviewFrag = "/places/tab/reviews/kakaomap/" + placeId;

        AtomicReference<String> panelJson = new AtomicReference<>();
        AtomicReference<String> reviewsJson = new AtomicReference<>();

        try (Page page = browser.newPage()) {
            page.onResponse(response -> {
                String url = response.url();
                if (response.status() == 200 && response.request().resourceType()
                    .matches("xhr|fetch")) {
                    if (url.contains(panelFrag) && panelJson.get() == null) {
                        panelJson.set(response.text());
                        log.debug("Captured panel JSON");
                    }
                    if (url.contains(reviewFrag) && reviewsJson.get() == null) {
                        reviewsJson.set(response.text());
                        log.debug("Captured reviews JSON");
                    }
                }
            });

            page.navigate(placeUrl + "#comment", new NavigateOptions()
                .setTimeout(30_000L)
                .setWaitUntil(WaitUntilState.NETWORKIDLE)
            );

            if (panelJson.get() == null || reviewsJson.get() == null) {
                throw new RuntimeException("panel or reviews JSON not captured");
            }

            JsonObject merged = new JsonObject();
            merged.add("info", JsonParser.parseString(panelJson.get()).getAsJsonObject());
            merged.add("reviews", JsonParser.parseString(reviewsJson.get()).getAsJsonObject());

            return merged;
        }
    }

    private String extractPlaceId(String placeUrl) {
        try {
            String path = URI.create(placeUrl).getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid placeUrl: " + placeUrl, e);
        }
    }

    public void close() {
        log.info("▶ Closing Playwright & Browser");
        browser.close();
        playwright.close();
    }
}
