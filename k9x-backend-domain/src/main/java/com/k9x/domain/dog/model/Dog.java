package com.k9x.domain.dog.model;

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

    public boolean belongsToSameOwner(String owner) {
        return this.owner != null ? this.owner.equals(owner) : this.creator.equals(owner);
    }

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
