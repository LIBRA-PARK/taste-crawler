package org.prography.crawler.kakao.dto.map.review;

import java.util.List;

public record Review(
    long reviewId,
    int starRating,
    String contents,
    int photoCount,
    List<Photo> photos,
    String status,
    List<Integer> strengthIds,
    String registeredAt,
    String updatedAt,
    ReviewMeta meta
) {

}
