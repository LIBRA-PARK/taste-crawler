package org.prography.crawler.kakao.dto.map;

import org.prography.crawler.kakao.dto.map.info.KakaoInfoResponse;
import org.prography.crawler.kakao.dto.map.review.KakaoReviewResponse;

public record KakaoMapResponse(
    KakaoInfoResponse info,
    KakaoReviewResponse review
) {

}
