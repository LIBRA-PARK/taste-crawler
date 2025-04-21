package org.prography;

import static com.mongodb.client.model.Filters.eq;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.UpdateOptions;
import java.util.ArrayList;
import java.util.List;
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
                // 2) Playwright로 리뷰+상세 데이터 가져오기
                JsonObject merged = PlaywrightManager
                    .instance()
                    .fetchKaKaoReviews(placeUrl);

                // 3) KAKAO_REVIEWS 컬렉션에 upsert
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
}