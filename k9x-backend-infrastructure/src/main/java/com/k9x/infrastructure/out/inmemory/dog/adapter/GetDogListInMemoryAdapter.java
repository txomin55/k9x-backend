package com.k9x.infrastructure.out.inmemory.dog.adapter;

import com.k9x.domain.commons.entitystatemachine.EntityStateMachine;
import com.k9x.domain.dog.model.Dog;
import com.k9x.domain.dog.port.GetDogListPersistencePort;
import com.k9x.infrastructure.out.inmemory.dog.entity.DogEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GetDogListInMemoryAdapter implements GetDogListPersistencePort {

    private final Map<String, DogEntity> simulatedDogMap;

    public GetDogListInMemoryAdapter(Map<String, DogEntity> simulatedDogMap) {
        this.simulatedDogMap = simulatedDogMap;
    }

    @Override
    public List<Dog> getDogs(String owner) {

        return simulatedDogMap
                .values()
                .stream()
                .filter(it -> it.getOwner().equals(owner))
                .map(dog -> new Dog(dog.getId(), dog.getName(), dog.getImage(), dog.getOwner(), EntityStateMachine.valueOfState(dog.getState()), dog.getLastUpdate()))
                .collect(Collectors.toList());
    }
}
