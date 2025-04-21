package org.prography.mongo;

import java.util.List;

public record BulkInsertResult(
    List<String> inserted, List<String> skipped) {

    public int insertedCount() {
        return inserted.size();
    }

    public int skippedCount() {
        return skipped.size();
    }
}
