package org.prography.crawler.kakao.dto.map.review;

public record OwnerMeta(
    String mapUserId,
    String nickname,
    String profileImageUrl,
    TimelineLevel timelineLevel,
    int reviewCount,
    int followerCount,
    double averageScore,
    String profileStatus,
    boolean isFollowing
) {

}
