package org.prography.geo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GeoRectSlice {

    private static class Keys {

        static final String FEATURES = "features";
        static final String PROPERTIES = "properties";
        static final String ADM_NM = "adm_nm";
        static final String GEOMETRY = "geometry";
        static final String TYPE = "type";
        static final String COORDINATES = "coordinates";
        static final String MULTI_POLYGON = "MultiPolygon";
    }

    private static final String RESOURCE_PATH = "HangJeongDong_ver20250401.geojson";
    private final JsonArray features;

    private static class Holder {

        private static final GeoRectSlice INSTANCE = new GeoRectSlice();
    }

    public static GeoRectSlice getInstance() {
        return Holder.INSTANCE;
    }

    private GeoRectSlice() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(RESOURCE_PATH)) {
            Objects.requireNonNull(is, "Resource not found: " + RESOURCE_PATH);
            try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                this.features = root.getAsJsonArray(Keys.FEATURES);
            }
        } catch (IOException | JsonIOException | JsonSyntaxException e) {
            throw new IllegalStateException("Failed to load/parse geojson: " + RESOURCE_PATH, e);
        }
    }

    /**
     * 특정 행정동(adm_nm)에 대한 geometry 영역을 rect로 나눈다.
     *
     * @param admName 대상 adm_nm (예: "서울특별시 종로구 사직동")
     * @param step    슬라이스 단위 (도 단위, approx. 거리 아래 표 참고)
     *                <ul>
     *                  <li>0.001 ≈ 100m</li>
     *                  <li>0.002 ≈ 200m</li>
     *                  <li>0.005 ≈ 500m</li>
     *                  <li>0.01  ≈ 1km</li>
     *                </ul>
     * @return rect 리스트 (rect = "서쪽,북쪽,동쪽,남쪽" = "x1,y2,x2,y1")
     */
    public List<String> sliceRectFromFeature(String admName, double step) {
        List<String> rects = new ArrayList<>();

        for (JsonElement elem : features) {
            JsonObject feature = elem.getAsJsonObject();
            JsonObject properties = feature.getAsJsonObject(Keys.PROPERTIES);

            if (!admName.equals(properties.get(Keys.ADM_NM).getAsString())) {
                continue;
            }

            JsonObject geometry = feature.getAsJsonObject(Keys.GEOMETRY);
            if (!geometry.has(Keys.COORDINATES)) {
                break;
            }

            // MultiPolygon 또는 Polygon 외곽선 좌표 추출
            JsonArray outer = extractOuterRing(geometry);

            // Bounding box 계산
            double minX = Double.POSITIVE_INFINITY,
                maxX = Double.NEGATIVE_INFINITY,
                minY = Double.POSITIVE_INFINITY,
                maxY = Double.NEGATIVE_INFINITY;

            for (JsonElement pt : outer) {
                JsonArray coord = pt.getAsJsonArray();
                double x = coord.get(0).getAsDouble();
                double y = coord.get(1).getAsDouble();
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }

            for (double x = minX; x < maxX; x += step) {
                for (double y = minY; y < maxY; y += step) {
                    double x2 = Math.min(x + step, maxX);
                    double y2 = Math.min(y + step, maxY);
                    rects.add("%.6f,%.6f,%.6f,%.6f".formatted(x, y2, x2, y));
                }
            }

            break;
        }
        return rects;
    }

    private static JsonArray extractOuterRing(JsonObject geometry) {
        String type = geometry.get(Keys.TYPE).getAsString();
        JsonArray coordinates = geometry.getAsJsonArray(Keys.COORDINATES);

        if (Keys.MULTI_POLYGON.equals(type)) {
            return coordinates.get(0).getAsJsonArray()
                .get(0).getAsJsonArray();
        } else {
            return coordinates.get(0).getAsJsonArray();
        }
    }
}
