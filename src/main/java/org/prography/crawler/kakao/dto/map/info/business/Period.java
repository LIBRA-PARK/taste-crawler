package org.prography.crawler.kakao.dto.map.info.business;

import java.util.List;

public record Period(
    String periodName,
    String periodType,
    boolean isDayBusinessHoursSameEveryday,
    List<DayBusinessHoursInfo> dayBusinessHoursInfos
) {

}
