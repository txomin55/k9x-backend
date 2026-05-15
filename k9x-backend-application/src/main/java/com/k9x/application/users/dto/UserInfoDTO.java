package com.k9x.application.users.dto;

public class UserInfoDTO {

    private final String id;
    private final String email;
    private final boolean organizer;

    public UserInfoDTO(String id, String email, boolean organizer) {
        this.id = id;
        this.email = email;
        this.organizer = organizer;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public boolean isOrganizer() {
        return organizer;
    }
}
