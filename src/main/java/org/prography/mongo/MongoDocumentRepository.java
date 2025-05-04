package org.prography.mongo;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.excludeId;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.MongoException;
import com.mongodb.bulk.BulkWriteError;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.bulk.BulkWriteUpsert;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.prography.crawler.kakao.dto.api.Document;
import org.prography.crawler.kakao.dto.api.KakaoLocalSearchResponse;
import org.prography.crawler.kakao.dto.map.KakaoMapResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDocumentRepository {

    private static final Logger log = LoggerFactory.getLogger(MongoDocumentRepository.class);
    private static final String MONGO_URI = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "crawler";
    private static final String KAKAO_API_COLLECTION = "KAKAO_INFO";
    private static final String KAKAO_REVIEW_COLLECTION = "KAKAO_REVIEW";

    private final MongoCollection<org.prography.crawler.kakao.dto.api.Document> kakaoDocumentCollection;
    private final MongoCollection<KakaoMapResponse> kakaoMapCollection;

    private final MongoClient client;
    private final MongoDatabase database;

    private static class Holder {

        private static final MongoDocumentRepository INSTANCE = new MongoDocumentRepository();

    }

    public static MongoDocumentRepository getInstance() {
        return Holder.INSTANCE;
    }

    private MongoDocumentRepository() {
        this.client = MongoClients.create(MONGO_URI);
        this.database = client.getDatabase(DATABASE_NAME);
        this.kakaoDocumentCollection = database.getCollection(KAKAO_API_COLLECTION,
            org.prography.crawler.kakao.dto.api.Document.class);
        this.kakaoMapCollection = database.getCollection(KAKAO_REVIEW_COLLECTION,
            KakaoMapResponse.class);
    }

    public List<String> findAllKakaoIds() {
        return kakaoDocumentCollection
            .find()
            .projection(excludeId())
            .map(Document::id)
            .into(new ArrayList<>());
    }

    public String findIdByName() {
        return Optional.ofNullable(kakaoDocumentCollection.find().first())
            .map(Document::id)
            .orElseThrow();
    }

    public void saveKakaoInfo(KakaoLocalSearchResponse response) {
        if (response.documents().isEmpty()) {
            return;
        }

        List<ReplaceOneModel<org.prography.crawler.kakao.dto.api.Document>> ops =
            response.documents().stream()
                .map(doc -> new ReplaceOneModel<>(
                    eq("_id", doc.toId()),
                    doc,
                    new ReplaceOptions().upsert(true)
                ))
                .toList();

        BulkWriteOptions opts = new BulkWriteOptions().ordered(false);

        try {
            BulkWriteResult bulkWriteResult = kakaoDocumentCollection.bulkWrite(ops, opts);

            for (BulkWriteUpsert upsert : bulkWriteResult.getUpserts()) {
                log.info("저장 성공 : {}", upsert.getId());
            }
        } catch (MongoBulkWriteException e) {
            List<BulkWriteError> errors = e.getWriteErrors();

            for (BulkWriteError error : errors) {
                log.error("저장 실패 : {}", error.getMessage());
            }

            if (e.getWriteConcernError() != null) {
                log.error("WriteConcernError: {}", e.getWriteConcernError());
            }
        }
    }

    public void saveKakaoReview(String id, KakaoMapResponse kakaoMapResponse) {
        ReplaceOneModel<KakaoMapResponse> model = new ReplaceOneModel<>(
            eq("_id", id),
            kakaoMapResponse,
            new ReplaceOptions().upsert(true)
        );

        try {
            kakaoMapCollection.replaceOne(
                Filters.eq("_id", id),
                kakaoMapResponse,
                new ReplaceOptions().upsert(true)
            );
        } catch (MongoException e) {
            log.error("카카오 리뷰 저장 실패 : _id[{}] / {}", id, kakaoMapResponse.info().summary().name());
            throw e;
        }
    }


    public void close() {
        client.close();
    }
}
