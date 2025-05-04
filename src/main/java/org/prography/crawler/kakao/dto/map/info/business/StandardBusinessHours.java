package org.prography.crawler.kakao.dto.map.info.business;

import java.util.List;

public record StandardBusinessHours(
    List<Period> periods
) {

}
