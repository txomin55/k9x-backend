package com.k9x.infrastructure.out.inmemory.dog.entity;

import com.k9x.domain.dog.model.Dog;

public record DogEntity(
        String id,
        String name,
        String image,
        String owner,
        String state,
        long lastUpdate
) {
    public DogEntity(Dog dog) {
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
