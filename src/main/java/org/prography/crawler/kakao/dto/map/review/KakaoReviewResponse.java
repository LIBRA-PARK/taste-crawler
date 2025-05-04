package org.prography.crawler.kakao.dto.map.review;

import java.util.List;
import org.prography.crawler.kakao.dto.map.review.score.ScoreSet;

public record KakaoReviewResponse(
    ScoreSet scoreSet,
    List<StrengthDescription> strengthDescription,
    List<Review> reviews,
    boolean hasNext,
    boolean hasMyReview
) {

}
