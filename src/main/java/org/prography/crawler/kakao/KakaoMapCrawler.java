package org.prography.crawler.kakao;

import java.util.ArrayList;
import java.util.List;
import org.prography.crawler.kakao.client.KakaoMapClient;
import org.prography.crawler.kakao.dto.map.KakaoMapResponse;
import org.prography.crawler.kakao.dto.map.info.KakaoInfoResponse;
import org.prography.crawler.kakao.dto.map.review.KakaoReviewResponse;
import org.prography.crawler.kakao.dto.map.review.Review;
import org.prography.crawler.kakao.dto.map.review.StrengthDescription;
import org.prography.crawler.kakao.dto.map.review.score.ScoreSet;
import org.prography.mongo.MongoDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KakaoMapCrawler {

    private static final Logger log = LoggerFactory.getLogger(KakaoMapCrawler.class);
    private static final MongoDocumentRepository repository = MongoDocumentRepository.getInstance();
    private final KakaoMapClient client;

    public KakaoMapCrawler() {
        this.client = new KakaoMapClient();
    }

    public void crawlReview() {
//        String id = repository.findIdByName();
        List<String> ids = repository.findAllKakaoIds();
        for (String id : ids) {
            KakaoInfoResponse info = client.callInfo(id);
            KakaoReviewResponse reviewResponse = client.callReview(id, 0);

            ScoreSet scoreSet = reviewResponse.scoreSet();
            List<StrengthDescription> strengthDescriptions = reviewResponse.strengthDescription();
            List<Review> reviews = reviewResponse.reviews() == null ? new ArrayList<>()
                : new ArrayList<>(reviewResponse.reviews());

            while (reviewResponse.hasNext()) {
                long lastReviewId = reviewResponse.reviews().getLast().reviewId();
                reviewResponse = client.callReview(id, lastReviewId);
                reviews.addAll(reviewResponse.reviews());
            }

            KakaoReviewResponse kakaoReviewResponse = new KakaoReviewResponse(
                scoreSet,
                strengthDescriptions,
                reviews,
                false, false);
            repository.saveKakaoReview(id, new KakaoMapResponse(info, kakaoReviewResponse));
        }
    }
}
