package org.prography.caller.client;

import java.util.List;

public record SameName(
    List<String> region,
    String keyword,
    String selectedRegion) {

}
