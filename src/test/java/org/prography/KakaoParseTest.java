package org.prography;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.prography.kakao.KakaoJsonParser;

class KakaoParseTest {

    private final String JSON_PATH = "temp.json";

    @Test
    @DisplayName("정상적으로 파싱 되는지 확인")
    void testParseDocumentsFromFile() throws Exception {
        Path jsonPath = Path.of(JSON_PATH);
        JsonObject jsonObject = JsonParser.parseString(Files.readString(jsonPath))
            .getAsJsonObject();
        List<JsonObject> docs = KakaoJsonParser.getDocuments(jsonObject);

        assertNotNull(docs);
        assertFalse(docs.isEmpty());
        JsonObject first = docs.getFirst();
        assertEquals("대전 중구 은행동 145-1", first.get("address_name").getAsString());
    }

    @Test
    @DisplayName("마지막 페이지 확인")
    void testIsEndPage() throws IOException {
        Path jsonPath = Path.of(JSON_PATH);
        JsonObject jsonObject = JsonParser.parseString(Files.readString(jsonPath))
            .getAsJsonObject();
        boolean endPage = KakaoJsonParser.isEndPage(jsonObject);

        assertFalse(endPage);
    }

    @Test
    @DisplayName("address_name 과 place_name 으로 아이디 만들기")
    void testMakeKey() throws Exception {
        Path jsonPath = Path.of(JSON_PATH);
        JsonObject jsonObject = JsonParser.parseString(Files.readString(jsonPath))
            .getAsJsonObject();
        List<JsonObject> docs = KakaoJsonParser.getDocuments(jsonObject);

        assertNotNull(docs);
        assertFalse(docs.isEmpty());
        JsonObject first = docs.getFirst();
        String id = KakaoJsonParser.toId(first, "address_name", "place_name");
        assertEquals("대전_중구_은행동_145-1,성심당_본점", id);
    }

    @Test
    @DisplayName("생성된 ID 와 파싱돤 Json 응답 값을 몽고 디비에 저장")
    void testInsertToMongoDB() {
        // 1) MongoDB 접속 (로컬)
        try (MongoClient client = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase db = client.getDatabase("crawler");
            MongoCollection<Document> coll = db.getCollection("KAKAO_INFO");

            Path jsonPath = Path.of(JSON_PATH);
            JsonObject jsonObject = JsonParser.parseString(Files.readString(jsonPath))
                .getAsJsonObject();
            List<JsonObject> docs = KakaoJsonParser.getDocuments(jsonObject);

            for (JsonObject doc : docs) {
                String id = KakaoJsonParser.toId(jsonObject, "address_name", "place_name");

                boolean exists = coll.find(eq("_id", id)).iterator().hasNext();
                if (exists) {
                    System.out.printf("Skip: [%s] 이미 존재함%n", id);
                    continue;
                }

                Document mongoDoc = new Document("_id", id)
                    .append("value", Document.parse(doc.toString()));

                coll.insertOne(mongoDoc);
                System.out.printf("Inserted: [%s]%n", id);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}