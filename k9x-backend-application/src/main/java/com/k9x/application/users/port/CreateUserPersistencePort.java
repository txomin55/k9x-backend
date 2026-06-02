package com.k9x.application.users.port;

public interface CreateUserPersistencePort {

    void createUser(String email, String image);
}
