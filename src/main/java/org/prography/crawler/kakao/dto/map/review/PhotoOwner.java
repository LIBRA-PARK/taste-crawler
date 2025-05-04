package org.prography.crawler.kakao.dto.map.review;

import java.util.List;

public record PhotoOwner(
    String mapUserId,
    String nickname,
    String imageUrl,
    TimelineLevel timelineLevel,
    int reviewCount,
    int followerCount,
    List<String> profileLinks,
    double averageScore,
    String profileStatus
) {

}
