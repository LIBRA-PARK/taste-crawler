package org.prography.crawler.kakao.dto.api;

import java.util.List;

public record KakaoLocalSearchResponse(
    PlaceMeta meta,
    List<Document> documents) {

}