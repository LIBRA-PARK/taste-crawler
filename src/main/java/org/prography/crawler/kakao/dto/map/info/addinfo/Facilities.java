package org.prography.crawler.kakao.dto.map.info.addinfo;

public record Facilities(
    boolean isReservation,
    boolean isDelivery,
    boolean isTakeout,
    boolean isWifi,
    boolean isPet,
    boolean isParking,
    boolean isFordisabled,
    boolean isSmokingroom
) {

}
