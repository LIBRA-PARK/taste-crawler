package org.prography.crawler.exception;

public class KakaoLocalApiException extends RuntimeException {

    public KakaoLocalApiException(String msg) {
        super(msg);
    }

    public KakaoLocalApiException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
