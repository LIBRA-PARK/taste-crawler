package org.prography.crawler.kakao.dto.map.info.business;

public record BusinessHours(
    String confirmId,
    boolean isExist,
    RealTimeInfo realTimeInfo,
    StandardInfo standardInfo
) {

}
