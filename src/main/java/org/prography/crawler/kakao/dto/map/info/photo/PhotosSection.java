package org.prography.crawler.kakao.dto.map.info.photo;

import java.util.List;

public record PhotosSection(
    PhotoCounts counts,
    List<PhotoSummary> photos
) {

}
