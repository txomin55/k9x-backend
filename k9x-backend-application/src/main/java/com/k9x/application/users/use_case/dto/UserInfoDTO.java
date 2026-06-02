package com.k9x.application.users.use_case.dto;

public class UserInfoDTO {

    private final String id;
    private final String email;
    private final String image;
    private final boolean organizer;

    public UserInfoDTO(String id, String email, String image, boolean organizer) {
        this.id = id;
        this.email = email;
        this.image = image;
        this.organizer = organizer;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getImage() {
        return image;
    }

    public boolean isOrganizer() {
        return organizer;
    }
}
