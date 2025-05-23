package org.prography.crawler.kakao.dto.api;

public record Document(
    String id,
    String placeName,
    String categoryName,
    String categoryGroupCode,
    String phone,
    String addressName,
    String roadAddressName,
    String x,
    String y,
    String PlaceUrl,
    String distance) {

    public String toId() {
        return addressName + "," + placeName;
    }
}
