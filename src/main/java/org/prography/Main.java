package org.prography;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.prography.geo.GeoRectSlice;
import org.prography.kakao.client.KakaoLocalApiClient;
import org.prography.kakao.crawler.KakaoLocalCrawler;
import org.prography.mongo.MongoDocumentRepository;
import org.slf4j.LoggerFactory;

public class Main {

    public static void main(String[] args) {
        Logger mongoLogger = (Logger) LoggerFactory.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.WARN);

        var client = new KakaoLocalApiClient();
        var repository = MongoDocumentRepository.getInstance();
        var rectSlice = GeoRectSlice.getInstance();
        new KakaoLocalCrawler(client, repository, rectSlice)
            .crawl(args[0]);
    }
}