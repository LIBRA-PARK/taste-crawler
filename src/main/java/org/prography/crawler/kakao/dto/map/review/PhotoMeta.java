package org.prography.crawler.kakao.dto.map.review;

public record PhotoMeta(
    PhotoOwner owner,
    boolean isLikedByMe,
    int viewCount,
    int likeCount,
    boolean near,
    Place place
) {

}
