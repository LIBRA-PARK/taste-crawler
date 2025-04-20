package org.prography;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.prography.geo.GeoRectSlice;

class RectTest {
    private final String GEO_JSON = "HangJeongDong_ver20250401.geojson";

    @Test
    @DisplayName(value = "GEO JSON 파싱")
    void parseGeoJson() throws IOException {
        Path geoPath = Path.of(GEO_JSON);
        String json = Files.readString(geoPath);

        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        JsonArray features = jsonObject.getAsJsonArray("features");

        System.out.println("📍 총 동 개수: " + features.size());

        for (JsonElement elem : features) {
            JsonObject feature = elem.getAsJsonObject();
            JsonObject properties = feature.getAsJsonObject("properties");

            String dongName = properties.get("adm_nm").getAsString();  // 동 이름
            String sigunguName = properties.has("sidonm") ? properties.get("sidonm").getAsString() : "";
            String fullName = sigunguName.isEmpty() ? dongName : sigunguName + " " + dongName;

            System.out.println(" - " + fullName);
        }
    }

    @Test
    @DisplayName(value = "행정 구역에 맞춰서 자르기")
    void sliceRect() throws IOException {
        Path geoPath = Path.of(GEO_JSON);
        String json = Files.readString(geoPath);

        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        JsonArray features = jsonObject.getAsJsonArray("features");

        List<String> rectList = GeoRectSlice.sliceRectFromFeature(features, "서울특별시 강남구 삼성1동");

        for (String s : rectList) {
            System.out.println(s);
        }
    }
}