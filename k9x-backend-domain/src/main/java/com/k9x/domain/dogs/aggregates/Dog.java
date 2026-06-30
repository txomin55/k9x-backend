package com.k9x.domain.dogs.aggregates;

public record Dog(
        String id,
        String identity,
        String breed,
        String name,
        String image,
        String owner,
        String handler,
        String creator,
        String country,
        String team,
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

    public String getImage() {
        return this.image;
    }

    public String getOwner() {
        return this.owner;
    }

    public String getHandler() {
        return this.handler;
    }

    public String getCreator() {
        return this.creator;
    }

    public String getCountry() {
        return this.country;
    }

    public String getTeam() {
        return this.team;
    }

    public String getIdentity() {
        return this.identity;
    }
}
