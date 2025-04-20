package org.prography;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.push;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import java.util.List;
import org.bson.Document;

public class Main {
    public static void main(String[] args) {
        // 1) MongoDB 접속 (로컬)
        try (MongoClient client = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase db = client.getDatabase("test");
            MongoCollection<Document> coll = db.getCollection("items");

            String key = "myKey";
            // 2) upsert: 키가 없으면 빈 문서로 생성
            coll.updateOne(
                eq("_id", key),
                new Document("$setOnInsert", new Document()),
                new UpdateOptions().upsert(true)
            );

            // 3) 반복문으로 4개의 JSON 객체를 values 배열에 추가
            Document[] docs = new Document[] {
                Document.parse("{\"foo\":7, \"bar\":\"A\"}"),
                Document.parse("{\"foo\":8, \"bar\":\"B\"}"),
                Document.parse("{\"foo\":9, \"bar\":\"C\"}"),
                Document.parse("{\"foo\":10, \"bar\":\"D\"}")
            };
            for (Document d : docs) {
                coll.updateOne(
                    eq("_id", key),
                    push("values", d)
                );
            }

            // 4) 최종 문서 조회 및 values 배열 출력
            Document result = coll.find(eq("_id", key)).first();
            if (result != null) {
                List<Document> values = result.getList("values", Document.class);
                System.out.println(">> values 배열 내용:");
                values.forEach(v -> System.out.println("  " + v.toJson()));
            } else {
                System.out.println("문서를 찾을 수 없습니다.");
            }
        }
    }
}