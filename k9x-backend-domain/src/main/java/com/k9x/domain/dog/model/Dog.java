package com.k9x.domain.dog.model;

import com.k9x.domain.commons.entitystatemachine.EntityStateMachine;
import com.k9x.domain.commons.exception.DomainException;
import com.k9x.domain.commons.exception.error.ErrorEnum;

public record Dog(
        String id,
        String name,
        String image,
        String owner,
        EntityStateMachine state,
        long lastUpdate
) {
    public Dog() {
        this(null, null, null, null, EntityStateMachine.ERROR, 0L);
    }

    public Dog(String id, String name, String image, String owner) {
        this(id, name, image, owner, EntityStateMachine.DRAFT, System.currentTimeMillis());
    }

    public Dog activate() {
        if (EntityStateMachine.DRAFT.equals(this.state)) {
            return new Dog(this.id, this.name, this.image, this.owner, EntityStateMachine.ACTIVE, this.lastUpdate);
        }

        throw new DomainException(ErrorEnum.UNAUTHORIZED_RESOURCE_STATE_TRANSITION_ERROR);
    }

    public boolean belongsToSameOwner(String owner) {
        if (this.isErrorState()) {
            return false;
        }

        return this.owner.equals(owner);
    }

    private boolean isErrorState() {
        return this.state == EntityStateMachine.ERROR;
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

    public EntityStateMachine getState() {
        return this.state;
    }

    public long getLastUpdate() {
        return this.lastUpdate;
    }
}
