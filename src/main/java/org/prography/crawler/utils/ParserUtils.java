package org.prography.crawler.utils;

import java.net.URI;

public final class ParserUtils {

    public static String extractKakaoPlaceId(String placeUrl) {
        try {
            String path = URI.create(placeUrl).getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid placeUrl: " + placeUrl, e);
        }
    }

}
