package org.prography.geo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public final class GeoRectSlice {

    public static List<String> sliceRectFromFeature(JsonArray features, String admName) {
        return sliceRectFromFeature(features, admName, 0.005);
    }

    /**
     * 특정 행정동(adm_nm)에 대한 geometry 영역을 rect로 나눈다.
     *
     * @param features 전체 GeoJSON Feature 배열
     * @param admName  대상 adm_nm (예: "서울특별시 종로구 사직동")
     * @param step     슬라이스 단위 (예: 0.005) = 대략 500 미터
     * @return rect 리스트 (rect = "x1,y2,x2,y1" 형식)
     */
    public static List<String> sliceRectFromFeature(JsonArray features, String admName, double step) {
        List<String> rectList = new ArrayList<>();

        for (JsonElement elem : features) {
            JsonObject feature = elem.getAsJsonObject();
            JsonObject properties = feature.getAsJsonObject("properties");

            if (!properties.get("adm_nm").getAsString().equals(admName))
                continue;

            JsonObject geometry = feature.getAsJsonObject("geometry");
            if (!geometry.has("coordinates")) continue;

            JsonArray outerRing = geometry.get("type").getAsString().equals("MultiPolygon")
                ? geometry.getAsJsonArray("coordinates").get(0).getAsJsonArray().get(0).getAsJsonArray()
                : geometry.getAsJsonArray("coordinates").get(0).getAsJsonArray();

            double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

            for (JsonElement point : outerRing) {
                JsonArray coord = point.getAsJsonArray();
                double x = coord.get(0).getAsDouble();
                double y = coord.get(1).getAsDouble();
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }

            for (double x = minX; x < maxX; x += step) {
                for (double y = minY; y < maxY; y += step) {
                    double x1 = x;
                    double y1 = y;
                    double x2 = Math.min(x + step, maxX);
                    double y2 = Math.min(y + step, maxY);
                    String rect = "%.6f,%.6f,%.6f,%.6f".formatted(x1, y2, x2, y1);
                    rectList.add(rect);
                }
            }
            break; // adm_nm 찾았으면 중단
        }

        return rectList;
    }
}
