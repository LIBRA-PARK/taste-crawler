package org.prography.crawler.exception;

public class KakaoMapRequestException extends RuntimeException {

    public KakaoMapRequestException(String msg) {
        super(msg);
    }

    public KakaoMapRequestException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
