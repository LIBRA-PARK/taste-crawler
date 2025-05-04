package org.prography.crawler.kakao.dto.map.info.summary;

public record RoadView(
    String panoId,
    int pan,
    int tilt,
    Point point,
    int level
) {

}
