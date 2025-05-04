package org.prography.crawler.kakao.dto.map.info.summary;

public record RoadView(
    String panoId,
    double pan,
    double tilt,
    Point point,
    double level
) {

}
