package org.prography.mongo;

import static com.mongodb.client.model.Filters.eq;

import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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
        // 존재 여부 체크
        if (coll.find(eq("_id", id)).first() != null) {
            // 이미 있음
            return false;
        }

        // 삽입
        Document mongoDoc = new Document("_id", id)
            .append("value", Document.parse(document.toString()));
        coll.insertOne(mongoDoc);
        return true;
    }

    public void close() {
        client.close();
    }
}
