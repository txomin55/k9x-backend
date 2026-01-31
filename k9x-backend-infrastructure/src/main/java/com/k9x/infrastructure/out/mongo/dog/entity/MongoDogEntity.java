package com.k9x.infrastructure.out.mongo.dog.entity;

import com.k9x.domain.dog.model.Dog;

import java.io.Serial;
import java.io.Serializable;

public record MongoDogEntity(
        String id,
        String name,
        String image,
        String owner,
        String state,
        long lastUpdate
) implements Serializable {

    @Serial
    private static final long serialVersionUID = -7595011183671841430L;

    public MongoDogEntity() {
        this(null, null, null, null, null, 0L);
    }

    public MongoDogEntity(Dog dog) {
        this(dog.getId(), dog.getName(), dog.getImage(), dog.getOwner(), dog.getState().name(), dog.getLastUpdate());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getOwner() {
        return owner;
    }

    public String getState() {
        return state;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }
}
