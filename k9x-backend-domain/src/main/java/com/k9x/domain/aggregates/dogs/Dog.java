package com.k9x.domain.aggregates.dogs;

public record Dog(
        String id,
        String identity,
        String breed,
        String name,
        String image,
        String owner,
        String creator,
        String country,
        String team,
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

    public String getImage() {
        return this.image;
    }

    public String getOwner() {
        return this.owner;
    }
}
