package com.spooky;

public class SearchSpecification {
    final private SearchType type;
    final private String value;
    final private boolean isCaseSensitive;

    SearchSpecification(SearchType type, String value, boolean isCaseSensitive) {
        this.type = type;
        this.value = value;
        this.isCaseSensitive = isCaseSensitive;
    }

    public String getValue() {
        return value;
    }

    public boolean isCaseSensitive() {
        return isCaseSensitive;
    }

}
