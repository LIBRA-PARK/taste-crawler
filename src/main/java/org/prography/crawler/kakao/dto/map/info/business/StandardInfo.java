package org.prography.crawler.kakao.dto.map.info.business;

public record StandardInfo(
    String contentProvider,
    boolean isContentProviderDisplay,
    StandardBusinessHours standardBusinessHours
) {

}
