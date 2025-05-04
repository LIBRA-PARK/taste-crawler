package org.prography.crawler.kakao.dto.map.info.business;

import java.util.List;

public record RealTimeInfo(
    BusinessHoursStatus businessHoursStatus,
    boolean isDayBusinessHoursSameEveryday,
    int currentStandardPeriodIndex,
    List<DayBusinessHoursInfo> dayBusinessHoursInfos
) {

}
