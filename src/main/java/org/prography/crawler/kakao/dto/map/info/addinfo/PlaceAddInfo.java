package org.prography.crawler.kakao.dto.map.info.addinfo;

import java.util.List;

public record PlaceAddInfo(
    Facilities facilities,
    List<TvProgram> tvPrograms,
    List<String> tags
) {

}
