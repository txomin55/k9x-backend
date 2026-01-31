package com.k9x.infrastructure.out.mongo.dog.adapter;

import com.k9x.domain.commons.entitystatemachine.EntityStateMachine;
import com.k9x.domain.dog.model.Dog;
import com.k9x.domain.dog.port.GetDogPersistencePort;
import com.k9x.infrastructure.out.mongo.dog.entity.MongoDogEntity;
import org.springframework.data.mongodb.core.MongoTemplate;

public class GetDogMongoAdapter implements GetDogPersistencePort {

    private final MongoTemplate mongoTemplate;

    public GetDogMongoAdapter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Dog getDog(String id) {

        MongoDogEntity dog = mongoTemplate.findById(id, MongoDogEntity.class);
        if(dog == null){
            return new Dog();
        }
        return new Dog(dog.getId(), dog.getName(), dog.getImage(), dog.getOwner(), EntityStateMachine.valueOfState(dog.getState()), dog.getLastUpdate());
    }
}

