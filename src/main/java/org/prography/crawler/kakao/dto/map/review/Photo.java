package org.prography.crawler.kakao.dto.map.review;

public record Photo(
    String url,
    long photoId,
    long reviewId,
    String updatedAt,
    PhotoMeta meta
) {

}
