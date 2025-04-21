package org.prography.kakao.crawler;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.prography.config.KakaoConfig;
import org.prography.geo.GeoRectSlice;
import org.prography.kakao.KakaoJsonParser;
import org.prography.kakao.client.KakaoLocalApiClient;
import org.prography.mongo.BulkInsertResult;
import org.prography.mongo.MongoDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KakaoLocalCrawler {

    private static final Logger log = LoggerFactory.getLogger(KakaoLocalCrawler.class);

    private final KakaoLocalApiClient client;
    private final MongoDocumentRepository repository;
    private final GeoRectSlice rectSlice;

    public KakaoLocalCrawler(KakaoLocalApiClient client, MongoDocumentRepository repository,
        GeoRectSlice rectSlice) {
        this.client = client;
        this.repository = repository;
        this.rectSlice = rectSlice;
    }

    public void crawl(String admName) {
        List<String> rects = rectSlice.sliceRectFromFeature(admName, 0.002);
        if (rects.isEmpty()) {
            log.info("해당 행정구역({})에 대한 RECT 가 존재하질 않습니다.", admName);
        }
        for (String rect : rects) {
            log.info("▶ 슬라이스 처리 시작: {}", rect);
            crawlOneRect(rect);
        }
        repository.close();
    }

    private void crawlOneRect(String rect) {
        int page = 1;
        int totalElements = 0;
        int totalInserted = 0;
        int totalSkipped = 0;

        while (true) {
            JsonObject response;
            try {
                response = client.callLocalSearchApi(rect, page);
            } catch (KakaoLocalApiClient.ApiException e) {
                log.error("API 호출 실패 (rect={}, page={}): {}", rect, page, e.getMessage());
                return;
            }

            List<JsonObject> docs = KakaoJsonParser.getDocuments(response);
            totalElements += docs.size();
            BulkInsertResult insertResult = repository.saveAllIfNotExists(docs,
                doc -> KakaoJsonParser.toId(doc, "address_name", "place_name"));

            totalInserted += insertResult.insertedCount();
            totalSkipped += insertResult.skippedCount();

            log.info("페이지 {}: 총={}, 저장={}, 중복={}",
                page,
                docs.size(),
                insertResult.insertedCount(),
                insertResult.skippedCount());

            for (String skippedId : insertResult.skipped()) {
                log.debug("중복 스킵 ID: {}", skippedId);
            }

            boolean isEnd = KakaoJsonParser.isEndPage(response);
            if (isEnd || page >= 45) {
                log.info(
                    "■ 완료: {} ▶ pages={} totalElements={} totalInserted={} totalSkipped={}",
                    rect, page, totalElements, totalInserted, totalSkipped);
                break;
            }

            page++;
            throttle(KakaoConfig.DEFAULT_THROTTLE_MS);
        }
    }

    private void throttle(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

}
