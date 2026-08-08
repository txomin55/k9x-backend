package com.k9x.domain.dogs.aggregates;

public record Dog(
        String identification,
        String origin,
        String license,
        String breed,
        String name,
        String image,
        String owner,
        String handler,
        String creator,
        String country,
        String team,
        Sex sex,
        Integer withersCm,
        Boolean threeFciGenerationsConfirmed,
        long lastUpdate,
        long createdAt,
        Long deletedAt
) {

    public String getIdentification() {
        return this.identification;
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

    public String getOrigin() {
        return this.origin;
    }

    public String getLicense() {
        return this.license;
    }

    public Sex getSex() {
        return this.sex;
    }

    public Integer getWithersCm() {
        return this.withersCm;
    }

    public Boolean getThreeFciGenerationsConfirmed() {
        return this.threeFciGenerationsConfirmed;
    }
}
