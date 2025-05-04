package org.prography.crawler.kakao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KakaoCrawlerTest {

    @Test
    @DisplayName(value = "리팩토링 카카오 크롤러 테스트")
    void test() {
        KakaoLocalApiCrawler crawler = new KakaoLocalApiCrawler("서울특별시 송파구 위례동", 0.002);
        crawler.crawlPlaceUrl();
    }

    @Test
    @DisplayName(value = "")
    void KAKAO_MAP_CRAWLER() {
        KakaoMapCrawler crawler = new KakaoMapCrawler();
        crawler.crawlReview();
    }
}