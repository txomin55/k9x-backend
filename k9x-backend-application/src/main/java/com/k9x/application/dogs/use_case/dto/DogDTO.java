package com.k9x.application.dogs.use_case.dto;

public record DogDTO(String id, String name, String image, Boolean owned, String creator, String country, String team,
                     String owner, String handler, String identity) {

}
