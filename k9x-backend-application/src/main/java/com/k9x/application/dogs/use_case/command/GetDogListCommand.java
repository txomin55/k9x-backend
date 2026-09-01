package com.k9x.application.dogs.use_case.command;

/**
 * Inbound query of the dog list: the ownership filters plus the optional text searches and page window.
 * A {@code null} {@link #size()} means "no pagination", i.e. the whole matching list in a single page.
 * {@link #name()} and {@link #identification()} are the two ends of a single search box: when both are
 * given a dog matches if either of them does.
 */
public record GetDogListCommand(boolean owned, boolean created, String name, String country, Integer page,
                                Integer size, String identification) {

    public static final GetDogListCommand ALL =
            new GetDogListCommand(false, false, null, null, null, null, null);
}
