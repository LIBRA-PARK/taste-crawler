package org.prography.mongo;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.bulk.BulkWriteError;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.bulk.BulkWriteUpsert;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import java.util.List;
import org.bson.Document;
import org.prography.crawler.kakao.dto.api.KakaoLocalSearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDocumentRepository {

    private static final Logger log = LoggerFactory.getLogger(MongoDocumentRepository.class);
    private static final String MONGO_URI = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "crawler";
    private static final String KAKAO_INFO_COLLECTION = "KAKAO_INFO";

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
    }


    public void saveKakaoInfo(KakaoLocalSearchResponse response) {
        if (response.documents().isEmpty()) {
            return;
        }

        List<Document> docs = response.documents().stream()
            .map(res -> new Document("_id", res.toId())
                .append("value", res)).toList();

        List<ReplaceOneModel<Document>> ops = docs.stream()
            .map(doc -> new ReplaceOneModel<>(
                eq("_id", doc.get("_id")),
                doc,
                new ReplaceOptions().upsert(true)
            ))
            .toList();

        BulkWriteOptions opts = new BulkWriteOptions().ordered(false);

        try {
            BulkWriteResult bulkWriteResult = database.getCollection(KAKAO_INFO_COLLECTION)
                .bulkWrite(ops, opts);

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

    public void close() {
        client.close();
    }
}
