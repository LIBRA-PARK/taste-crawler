package org.prography.config;

public final class KakaoConfig {

    private KakaoConfig() {
    }

    public static final String API_KEY =
        System.getenv().getOrDefault("KAKAO_REST_API_KEY", "Kakao AK temp");
    public static final String BASE_URL =
        "https://dapi.kakao.com/v2/local/search/category.json";
    public static final String CATEGORY_GROUP = "FD6";
    public static final int DEFAULT_SIZE = 15;
    public static final long DEFAULT_THROTTLE_MS = 1_000;  // 1초
}
