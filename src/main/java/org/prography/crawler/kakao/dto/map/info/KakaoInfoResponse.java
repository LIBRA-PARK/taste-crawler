package org.prography.crawler.kakao.dto.map.info;

import org.prography.crawler.kakao.dto.map.info.addinfo.PlaceAddInfo;
import org.prography.crawler.kakao.dto.map.info.business.BusinessHours;
import org.prography.crawler.kakao.dto.map.info.photo.PhotosSection;
import org.prography.crawler.kakao.dto.map.info.summary.Summary;

public record KakaoInfoResponse(
    Summary summary,
    BusinessHours businessHours,
    PhotosSection photos,
    PlaceAddInfo placeAddInfo
) {

}
