package org.prography.crawler.kakao.dto.api;

public record PlaceMeta(
    int totalCount,
    int pageableCount,
    boolean isEnd,
    SameName sameName) {

}
