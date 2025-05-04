package org.prography.crawler.kakao.dto.map.info.business;

public record DayBusinessHoursInfo(
    String dayOfTheWeek,
    String date,
    boolean isOpenOffExist,
    boolean isHoliday,
    DayTime dayTime
) {

}
