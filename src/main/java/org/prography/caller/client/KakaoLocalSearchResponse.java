package org.prography.caller.client;

import java.util.List;

public record KakaoLocalSearchResponse(
    PlaceMeta meta,
    List<Document> documents) {

}