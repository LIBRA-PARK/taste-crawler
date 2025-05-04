package org.prography.crawler.config;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class RequestConfig {

    private final List<String> userAgents = List.of(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64)…",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)…",
        "curl/7.79.1"
    );
    private final ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor();

    private final Random rnd = new Random();
}
