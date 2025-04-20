package org.prography.kakao;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class KakaoJsonParser {

    private KakaoJsonParser() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * JsonObject 에 있는 "documents" 배열을 파싱해서 JsonObject 리스트로 반환합니다.
     *
     * @param jsonObject Gson 을 통해서 생성된 JsonObject
     * @return documents 배열의 각 요소를 JsonObject로 담은 리스트
     */
    public static List<JsonObject> getDocuments(JsonObject jsonObject) {
        if (jsonObject.isEmpty()) {
            return List.of();
        }

        JsonArray docsArray = jsonObject.getAsJsonArray("documents");
        List<JsonObject> documents = new ArrayList<>();
        for (JsonElement elem : docsArray) {
            documents.add(elem.getAsJsonObject());
        }
        return documents;
    }

    /**
     * JsonObject 에 있는 'meta' 값을 파싱해서 해당 응답값에 대한 페이지가 마지막인지 체크
     *
     * @param jsonObject JSON 전체 문자열
     * @return 마지막일 경우 true
     */
    public static boolean isEndPage(JsonObject jsonObject) {
        if (!jsonObject.has("meta")) {
            return false;
        }
        JsonObject meta = jsonObject.getAsJsonObject("meta");

        return meta.has("is_end") && !meta.get("is_end").isJsonNull() && meta.get("is_end")
            .getAsBoolean();
    }

    /**
     * JSON Object 에서 특정 key 를 통해서 id 반환
     *
     * @param obj  API 응답된 JsonObject
     * @param keys ID 생성에 필요한 key 들의 가변인수
     * @return 공백을 대시로 변경하고 서로 다른 key 에 대해선 콤마를 통해 구분한 ID 반환
     */
    public static String toId(JsonObject obj, String... keys) {
        return Arrays.stream(keys)
            .map(k -> obj.get(k).getAsString().replace(" ", "_"))
            .collect(Collectors.joining(","));
    }

}
