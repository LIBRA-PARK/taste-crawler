package org.prography.crawler.kakao.dto.map.review.score;

import java.util.List;

public record ScoreSet(
    int reviewCount,
    int totalScore,
    double averageScore,
    List<StrengthCount> strengthCounts,
    int strengthUv
) {

}
