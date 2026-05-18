package com.k9x.application.competitions.use_case.command;

public record UpdateCompetitionCommand(String name, String description, String country, String address) {
}
