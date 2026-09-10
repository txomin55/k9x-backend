package com.k9x.application.dogs.port.payload;

import com.k9x.application.dogs.use_case.command.GetPublicDogListCommand;

/**
 * What the persistence side needs to resolve the public dog directory: the text searches and the page window
 * translated to {@code offset}/{@code limit}. A {@code null} {@code limit} means the whole list is fetched.
 * The searches narrow the list down together, not as alternatives: a dog is listed only when every provided
 * filter matches it.
 */
public record PublicDogListFilter(String nameContains, String handlerContains, String country, Integer offset,
                                  Integer limit) {

    public static PublicDogListFilter from(GetPublicDogListCommand command) {
        String name = blankToNull(command.name());
        String handler = blankToNull(command.handler());
        String country = blankToNull(command.country());
        if (command.size() == null) {
            return new PublicDogListFilter(name, handler, country, null, null);
        }
        int page = command.page() == null ? 0 : command.page();
        return new PublicDogListFilter(name, handler, country, page * command.size(), command.size());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public boolean paginated() {
        return limit != null;
    }

    public int page() {
        return paginated() ? offset / limit : 0;
    }
}
