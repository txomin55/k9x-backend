package com.k9x.application.dogs.command;

public record UpdateDogCommand(String name, String image, String breed, String identity, String owner, String team, String country) {
}
