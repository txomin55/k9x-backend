package com.k9x.domain.commons.entitystatemachine;

public enum EntityStateMachine {
    DRAFT("DRAFT"),
    ACTIVE("ACTIVE"),
    ERROR("ERROR"),
    DELETED("DELETED");

    private final String state;

    EntityStateMachine(String state) {
        this.state = state;
    }

    public static EntityStateMachine valueOfState(String state) {
        for (EntityStateMachine e : values()) {
            if (e.state.equals(state)) {
                return e;
            }
        }
        return null;
    }
}
