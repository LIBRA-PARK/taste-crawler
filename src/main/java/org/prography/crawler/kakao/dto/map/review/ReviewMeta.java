package org.prography.crawler.kakao.dto.map.review;

import java.util.List;

public record ReviewMeta(
    OwnerMeta owner,
    boolean isLikedByMe,
    boolean isOwnerMe,
    int likeCount,
    List<String> likeUserProfileImageList,
    boolean isPlaceOwnerPick,
    Place place
) {

}
