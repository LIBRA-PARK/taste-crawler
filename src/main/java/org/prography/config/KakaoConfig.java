package org.prography.config;

public final class KakaoConfig {

    private KakaoConfig() {
    }

    public static final String KAKAO_API_KEY =
        System.getenv().getOrDefault("KAKAO_REST_API_KEY", "KakaoAK temp");
}
