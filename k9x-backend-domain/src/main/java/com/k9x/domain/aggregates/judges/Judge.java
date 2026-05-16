package com.k9x.domain.aggregates.judges;

public record Judge(
        String id,
        String name,
        String creator,
        long lastUpdate,
        long createdAt,
        long deletedAt
) {

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getCreator() {
        return this.creator;
    }
}
