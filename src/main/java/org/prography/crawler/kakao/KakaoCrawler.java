package org.prography.crawler.kakao;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.prography.caller.KakaoLocalApiCaller;
import org.prography.caller.KakaoLocalApiCaller.KakaoLocalApiException;
import org.prography.caller.client.KakaoLocalSearchResponse;
import org.prography.geo.GeoRectSlice;
import org.prography.mongo.MongoDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KakaoCrawler {

    private static final Logger log = LoggerFactory.getLogger(KakaoCrawler.class);
    private static final MongoDocumentRepository repository = MongoDocumentRepository.getInstance();
    private static final double DEFAULT_STEP = 0.005; // 약 500m
    private static final long DEFAULT_THROTTLE_SECONDS = 10;  // 10초
    private static final String KAKAO_INFO_PREFIX = "/places/panel3/";
    private static final String KAKAO_REVIEW_PREFIX = "/places/tab/reviews/kakaomap/";

    private final String admName;
    private final KakaoLocalApiCaller caller;
    private final List<String> rectList;


    public KakaoCrawler(String adnName, double step) {
        GeoRectSlice slice = GeoRectSlice.getInstance();
        this.admName = adnName;
        this.rectList = slice.sliceRectFromFeature(adnName, step);
        this.caller = new KakaoLocalApiCaller();
    }

    public KakaoCrawler(String admName) {
        this(admName, DEFAULT_STEP);
    }

    public void crawlPlaceUrl() {
        if (rectList.isEmpty()) {
            log.info("해당 행정구역[{}]에 대한 RECT가 존재하지 않습니다.", admName);
        }

        for (String rect : rectList) {
            log.info("슬라이스 처리 : {}", rect);
            int page = 1;

            while (true) {
                KakaoLocalSearchResponse response;
                try {
                    response = caller.call(rect, page);
                } catch (KakaoLocalApiException e) {
                    log.error("API 호출 실패 (rect={}, page={}): {}", rect, page, e.getMessage());
                    break;
                }
                
                repository.saveKakaoInfo(response);

                if (response.meta().isEnd() || page >= 45) {
                    break;
                }

                page++;
                throttle(DEFAULT_THROTTLE_SECONDS);
            }
        }

        repository.close();
    }

    public void crawlReview() {

    }

    private void throttle(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

}
