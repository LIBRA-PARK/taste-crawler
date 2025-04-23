package org.prography;

import static com.mongodb.client.model.Filters.eq;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.NavigateOptions;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.RequestOptions;
import com.microsoft.playwright.options.WaitUntilState;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.UpdateOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.prography.kakao.crawler.PlaywrightManager;

class PlaywrightTest {

    private static MongoClient mongoClient;
    private static MongoDatabase db;
    private static MongoCollection<Document> infoColl;
    private static MongoCollection<Document> reviewColl;

    @BeforeAll
    static void setup() {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        db = mongoClient.getDatabase("crawler");
        infoColl = db.getCollection("KAKAO_INFO");
        reviewColl = db.getCollection("KAKAO_REVIEW");
    }

    @AfterAll
    static void teardown() {
        PlaywrightManager.instance().close();
        mongoClient.close();
    }


    @Test
    @DisplayName(value = "동적 렌더링 후 네트워크 리소스 반환")
    void testDynamicRender() {
        PlaywrightManager pwMgr = PlaywrightManager.instance();

        JsonObject body = pwMgr.fetchKaKaoReviews("http://place.map.kakao.com/8117481");

        System.out.println(body);
        pwMgr.close();
    }

    @Test
    void fetchAndStoreReviewsForRandomPlaces() {
        // 1) KAKAO_INFO에서 랜덤 3개 문서 샘플링
        List<Document> samples = infoColl.aggregate(
            List.of(Aggregates.sample(3))
        ).into(new ArrayList<>());

        for (Document infoDoc : samples) {
            String docId = infoDoc.getString("_id");
            Document value = infoDoc.get("value", Document.class);
            String placeUrl = value.getString("place_url");

            try {
                JsonObject merged = PlaywrightManager
                    .instance()
                    .fetchKaKaoReviews(placeUrl);
                reviewColl.updateOne(
                    eq("_id", docId),
                    new Document("$set", new Document("data", Document.parse(merged.toString()))),
                    new UpdateOptions().upsert(true)
                );

                System.out.printf("✔ Saved reviews for %s%n", docId);

            } catch (RuntimeException e) {
                System.err.printf("✘ Failed for %s (%s): %s%n",
                    docId, placeUrl, e.getMessage());
            }
        }
    }

    private JsonObject stripReviewMeta(JsonObject rawJson) {
        if (!rawJson.has("reviews") || !rawJson.get("reviews").isJsonArray()) {
            return rawJson;
        }
        JsonArray reviews = rawJson.getAsJsonArray("reviews");
        for (JsonElement el : reviews) {
            if (el.isJsonObject()) {
                el.getAsJsonObject().remove("meta");
            }
        }
        return rawJson;
    }

    @Test
    @DisplayName(value = "NAVER 동적 렌더링 크롤링 테스트")
    void fetchNaverReveiw() throws InterruptedException {
        try (Playwright pw = Playwright.create()) {
            Browser browser = pw.chromium().launch(new LaunchOptions()
                .setHeadless(false)
            );
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            Response targetResp = page.waitForResponse(
                resp -> {
                    if (!resp.url().contains("/graphql")) {
                        return false;
                    }
                    String post = resp.request().postData();
                    if (post == null || !post.contains("visitorReviews")) {
                        return false;
                    }
                    String bizIds = resp.headers().get("x-gql-businessids");
                    return "65729586".equals(bizIds);
                },
                () -> {
                    page.navigate(
                        "https://map.naver.com/p/smart-around/place/65729586?placePath=/review");
                }
            );

            System.out.println("▶️ Request Payload:\n" + targetResp.request().postData());

            Map<String, String> hdr = targetResp.headers();
            System.out.println("⬅️ Headers:");
            System.out.println("   X-Gql-BusinessIds: " + hdr.get("x-gql-businessids"));
            System.out.println("   X-Gql-Query-Names: " + hdr.get("x-gql-query-names"));

            System.out.println("⬅️ Body:\n" + targetResp.text());

            browser.close();
        }
    }

    @Test
    @DisplayName(value = "네이버 PK ID 조회 하는 테스트")
    void getPKId() {
        try (Playwright pw = Playwright.create()) {
            // 1) APIRequestContext 준비 (Base URL, 필요 헤더 세팅)
            APIRequestContext api = pw.request().newContext(new APIRequest.NewContextOptions()
                    .setBaseURL("https://map.naver.com")
                // .setExtraHTTPHeaders(...) 필요하다면 여기에
            );

            // 2) GET 호출 – 파라미터를 map 으로 전달
            APIResponse res = api.get("/p/api/search/allSearch", RequestOptions.create()
                .setData(Map.of(
                    "query",       "서울 동대문구 장한로 161 치악산 횡연소요리전문점",
                    "type",        "all",
                    "searchCoord", "127.07174110000193;37.574837899999366"
                ))
            );

            // 3) 결과 확인
            System.out.println("Status: " + res.status());   // 200 이면 OK
            System.out.println("Body: " + res.text());      // JSON 내용
        }
    }

    @Test
    @DisplayName(value = "NAVER 동적 렌더링 크롤링 테스트")
    void fetchSearch() throws InterruptedException {
        final String script = """
                    Object.defineProperty(navigator, 'webdriver', { get: () => false });
                    window.chrome = { runtime: {} };
                    Object.defineProperty(navigator, 'languages', { get: () => ['ko-KR', 'ko'] });
                    Object.defineProperty(navigator, 'plugins', { get: () => [1,2,3,4,5] });
            """;
        String navigateUrl = "https://map.naver.com/p/search/서울_강남구_신사동_587-14,루위";
        String apiPrefix = "https://map.naver.com/p/api/search/allSearch";

        try (
            Playwright playwright = Playwright.create();
            Browser browser = playwright.chromium().launch(new LaunchOptions()
                .setHeadless(true)
                .setArgs(java.util.List.of(
                    "--disable-blink-features=AutomationControlled",
                    "--no-sandbox",
                    "--disable-setuid-sandbox"
                ))
            );
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/114.0.5735.133 Safari/537.36"
                )
                .setLocale("ko-KR")
                .setTimezoneId("Asia/Seoul")
                .setViewportSize(1920, 1080)
            );

        ) {
            context.addInitScript(script);
            Page page = context.newPage();

            Response resp = page.waitForResponse(
                r -> r.ok() && r.request().resourceType().matches("xhr|fetch") && r.url()
                    .startsWith(apiPrefix),
                () -> page.navigate(navigateUrl, new NavigateOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                    .setTimeout(30_000L))
            );

            JsonObject root = JsonParser.parseString(resp.text()).getAsJsonObject();
            JsonArray list = root
                .getAsJsonObject("result")
                .getAsJsonObject("place")
                .getAsJsonArray("list");

            if (!list.isEmpty()) {
                JsonElement first = list.get(0);
                System.out.println("▶ list[0] as JSON String:");
                System.out.println(first.toString());
            } else {
                System.out.println("⚠️ list 배열이 비어있습니다.");
            }

        } catch (PlaywrightException e) {
            System.err.println("❌ Playwright error: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            System.err.println("❌ JSON parse error: " + e.getMessage());
        }
    }
}