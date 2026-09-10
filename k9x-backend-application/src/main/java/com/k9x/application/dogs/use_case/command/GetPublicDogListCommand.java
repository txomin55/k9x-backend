package com.k9x.application.dogs.use_case.command;

/**
 * Inbound query of the public dog directory: the optional text searches, the country and the page window.
 * A {@code null} {@link #size()} means "no pagination", i.e. the whole matching list in a single page.
 * Unlike {@link GetDogListCommand} it carries no ownership filters: the directory is anonymous, so there is
 * no user to narrow the list down to.
 */
public record GetPublicDogListCommand(String name, String handler, String country, Integer page, Integer size) {

    public static final GetPublicDogListCommand ALL = new GetPublicDogListCommand(null, null, null, null, null);
}
