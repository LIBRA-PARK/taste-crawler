package org.prography;

import com.google.gson.JsonObject;
import java.util.List;
import org.prography.geo.GeoRectSlice;
import org.prography.kakao.KakaoJsonParser;
import org.prography.kakao.KakaoLocalSearch;
import org.prography.mongo.MongoDocumentRepository;

public class Main {

    public static void main(String[] args) {
        KakaoLocalSearch search = new KakaoLocalSearch();
        MongoDocumentRepository repo = MongoDocumentRepository.getInstance();
        GeoRectSlice slice = GeoRectSlice.getInstance();
        List<String> rectList = slice.sliceRectFromFeature("서울특별시 강남구 삼성1동");

        int page = 1;
        for (String rect : rectList) {
            JsonObject jsonObject = search.callLocalSearchApi(rect, page);
            if (!KakaoJsonParser.isEndPage(jsonObject)) {
                List<JsonObject> documents = KakaoJsonParser.getDocuments(jsonObject);
                for (JsonObject document : documents) {
                    String id = KakaoJsonParser.toId(document, "address_name", "place_name");
                    boolean inserted = repo.saveIfNotExists(id, document);
                    if (inserted) {
                        System.out.printf("Inserted: [%s]%n", id);
                    } else {
                        System.out.printf("Skip (exists): [%s]%n", id);
                    }

                }
                page++;
            } else {
                page = 1;
            }
        }
    }
}