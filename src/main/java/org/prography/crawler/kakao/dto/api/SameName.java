package org.prography.crawler.kakao.dto.api;

import java.util.List;

public record SameName(
    List<String> region,
    String keyword,
    String selectedRegion) {

}
