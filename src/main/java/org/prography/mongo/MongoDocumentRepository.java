package org.prography.mongo;

import static com.mongodb.client.model.Filters.eq;

import com.google.gson.JsonObject;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.bson.Document;

public class MongoDocumentRepository {

    private static final String MONGO_URI = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "crawler";
    private static final String COLLECTION_NAME = "KAKAO_INFO";

    private final MongoClient client;
    private final MongoCollection<Document> coll;

    private static class Holder {

        private static final MongoDocumentRepository INSTANCE = new MongoDocumentRepository();
    }

    public static MongoDocumentRepository getInstance() {
        return Holder.INSTANCE;
    }

    private MongoDocumentRepository() {
        this.client = MongoClients.create(MONGO_URI);
        MongoDatabase db = client.getDatabase(DATABASE_NAME);
        this.coll = db.getCollection(COLLECTION_NAME);
    }

    /**
     * 주어진 id가 없을 때만 해당 JsonObject를 MongoDB에 저장합니다.
     *
     * @param id       _id로 사용할 고유 문자열
     * @param document 저장할 JsonObject
     * @return true if inserted, false if skipped due to duplicate
     */
    public boolean saveIfNotExists(String id, JsonObject document) {
        if (coll.find(eq("_id", id)).first() != null) {
            return false;
        }

        Document mongoDoc = new Document("_id", id)
            .append("value", Document.parse(document.toString()));
        coll.insertOne(mongoDoc);
        return true;
    }

    /**
     * docs 리스트를 _id 기준 중복 없이 한 번에 저장합니다.
     *
     * @param docs        JsonObject 리스트
     * @param idExtractor 각 JsonObject에서 _id 문자열을 뽑아낼 함수
     * @return 벌크 결과 객체
     */
    public BulkInsertResult saveAllIfNotExists(List<JsonObject> docs,
        Function<JsonObject, String> idExtractor) {
        List<WriteModel<Document>> models = new ArrayList<>();
        List<String> ids = new ArrayList<>();

        for (JsonObject doc : docs) {
            String id = idExtractor.apply(doc);
            ids.add(id);
            Document d = new Document("_id", id)
                .append("value", Document.parse(doc.toString()));

            models.add(new UpdateOneModel<>(
                Filters.eq("_id", id),
                new Document("$setOnInsert", d),
                new UpdateOptions().upsert(true)
            ));
        }

        if (models.isEmpty()) {
            return new BulkInsertResult(List.of(), List.of());
        }

        BulkWriteResult result = coll.bulkWrite(models,
            new BulkWriteOptions().ordered(false));

        List<String> inserted = result.getUpserts().stream()
            .map(u -> u.getId().asString().getValue())
            .toList();

        List<String> skipped = ids.stream()
            .filter(id -> !inserted.contains(id))
            .toList();

        return new BulkInsertResult(inserted, skipped);
    }

    public void close() {
        client.close();
    }
}
