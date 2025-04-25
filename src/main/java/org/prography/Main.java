package org.prography;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.prography.caller.KakaoLocalApiCaller;
import org.prography.caller.client.KakaoLocalApiClient;
import org.prography.geo.GeoRectSlice;
import org.prography.mongo.MongoDocumentRepository;
import org.slf4j.LoggerFactory;

public class Main {

    public static void main(String[] args) {
//        if (args.length != 2) {
//            System.err.println("Usage: <행정구역> <KAKAO_API_KEY>");
//            System.exit(1);
//        }

        Logger mongoLogger = (Logger) LoggerFactory.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.WARN);

        var client = new KakaoLocalApiClient();
        var repository = MongoDocumentRepository.getInstance();
        var rectSlice = GeoRectSlice.getInstance();
        new KakaoLocalApiCaller(client, repository, rectSlice)
            .crawl("서울특별시 강남구 역삼1동");
//            .crawl(args[0]);
    }
}