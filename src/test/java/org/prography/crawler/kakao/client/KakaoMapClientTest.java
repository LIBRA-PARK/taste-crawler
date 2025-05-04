package org.prography.crawler.kakao.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.prography.crawler.kakao.dto.map.info.KakaoInfoResponse;
import org.prography.crawler.kakao.dto.map.review.KakaoReviewResponse;

class KakaoMapClientTest {

    @Test
    @DisplayName(value = "카카오 리뷰 요청")
    void PLACE_REVIEW_REQUEST() {
        KakaoMapClient client = new KakaoMapClient();
        KakaoReviewResponse response = client.callReview("17733090", 0L);

        Assertions.assertFalse(response.reviews().isEmpty());
    }

    @Test
    @DisplayName(value = "카카오 가게 정보 요청")
    void PLACE_INFO_REQUEST() {
        KakaoMapClient client = new KakaoMapClient();
        KakaoInfoResponse response = client.callInfo("17733090");

        Assertions.assertNotNull(response.summary().name());
    }

}