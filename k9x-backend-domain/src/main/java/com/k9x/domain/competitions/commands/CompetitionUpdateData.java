package com.k9x.domain.competitions.commands;

public record CompetitionUpdateData(String name, String description, String country, String address,
                                    Double coordAlt, Double coordLong) {
}
