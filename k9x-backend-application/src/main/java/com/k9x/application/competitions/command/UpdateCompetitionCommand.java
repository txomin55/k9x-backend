package com.k9x.application.competitions.command;

public record UpdateCompetitionCommand(String name, String description, String country, String address) {
}
