package org.prography.crawler.kakao.dto.map.info.photo;

public record PhotoSummary(
    String confirmId,
    String url,
    String type,
    String registeredAt,
    String updatedAt,
    String title
) {

}
