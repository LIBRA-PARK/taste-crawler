package org.prography.crawler.kakao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KakaoCrawlerTest {

    @Test
    @DisplayName(value = "리팩토링 카카오 크롤러 테스트")
    void test() {
        KakaoCrawler crawler = new KakaoCrawler("서울특별시 강남구 삼성2동", 0.002);
        crawler.crawlPlaceUrl();
    }
}