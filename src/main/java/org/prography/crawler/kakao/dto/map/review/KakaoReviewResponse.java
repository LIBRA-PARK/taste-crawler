package org.prography.crawler.kakao.dto.map.review;

import java.util.List;

public record KakaoReviewResponse(
    List<StrengthDescription> strengthDescription,
    List<Review> reviews,
    boolean hasNext,
    boolean hasMyReview
) {

}
