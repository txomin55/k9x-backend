package com.k9x.domain.judges.aggregates;

public record Judge(
        String id,
        String name,
        String creator,
        String country,
        long lastUpdate,
        long createdAt,
        Long deletedAt
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

    public String getCountry() {
        return this.country;
    }
}
