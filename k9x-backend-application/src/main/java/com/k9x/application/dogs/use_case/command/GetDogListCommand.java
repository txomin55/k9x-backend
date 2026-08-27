package com.k9x.application.dogs.use_case.command;

/**
 * Inbound query of the dog list: the ownership filters plus the optional name search and page window.
 * A {@code null} {@link #size()} means "no pagination", i.e. the whole matching list in a single page.
 */
public record GetDogListCommand(boolean owned, boolean created, String name, Integer page, Integer size) {

    public static final GetDogListCommand ALL = new GetDogListCommand(false, false, null, null, null);
}
