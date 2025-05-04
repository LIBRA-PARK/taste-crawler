package org.prography.crawler.kakao.dto.map.info.summary;

import java.util.List;

public record Summary(
    SummaryMeta meta,
    String confirmId,
    String name,
    Category category,
    Point point,
    Address address,
    RoadView roadView,
    List<Region> regions,
    List<String> homepages,
    List<PhoneNumber> phoneNumbers,
    List<Payment> payments) {

}
