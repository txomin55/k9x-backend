package com.k9x.application.dogs.port.payload;

import com.k9x.application.dogs.use_case.command.GetDogListCommand;

/**
 * What the persistence side needs to resolve a dog list: the ownership filters already resolved to
 * concrete user ids by the service case, the name search, and the page window translated to
 * {@code offset}/{@code limit}. A {@code null} {@code limit} means the whole list is fetched.
 */
public record DogListFilter(String owner, String creator, String nameContains, Integer offset, Integer limit) {

    public static DogListFilter from(String owner, String creator, GetDogListCommand command) {
        String name = command.name() == null || command.name().isBlank() ? null : command.name().trim();
        if (command.size() == null) {
            return new DogListFilter(owner, creator, name, null, null);
        }
        int page = command.page() == null ? 0 : command.page();
        return new DogListFilter(owner, creator, name, page * command.size(), command.size());
    }

    public boolean paginated() {
        return limit != null;
    }

    public int page() {
        return paginated() ? offset / limit : 0;
    }
}
