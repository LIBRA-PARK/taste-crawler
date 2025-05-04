package org.prography.config;

import java.util.List;

public class RequestHeaders {

    private RequestHeaders() {

    }

    public static final String AUTHORIZATION = "Authorization";
    public static final String ACCEPT = "Accept";
    public static final String APPLICATION_JSON = "application/json";
    public static final String USER_AGENT = "User-Agent";
    public static final String REFERER = "Referer";
    public static final String KAKAO_PF = "pf";
    public static final String WEB = "web";
    public static final List<String> USER_AGENTS = List.of(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.4 Safari/605.1.15",
        "Mozilla/5.0 (Linux; Android 12; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Mobile Safari/537.36",
        "curl/7.79.1"
    );
}
