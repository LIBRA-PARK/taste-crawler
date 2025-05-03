package org.prography.caller.client;

public record PlaceMeta(
    int totalCount,
    int pageableCount,
    boolean isEnd,
    SameName sameName) {
    
}
