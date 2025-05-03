package org.prography;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    public static void main(String[] args) {
//        if (args.length != 2) {
//            System.err.println("Usage: <행정구역> <KAKAO_API_KEY>");
//            System.exit(1);
//        }

        Logger mongoLogger = (Logger) LoggerFactory.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.WARN);

//            .crawl(args[0]);
    }
}