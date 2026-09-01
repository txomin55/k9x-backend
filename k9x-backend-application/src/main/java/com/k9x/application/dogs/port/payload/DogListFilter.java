package com.k9x.application.dogs.port.payload;

import com.k9x.application.dogs.use_case.command.GetDogListCommand;

/**
 * What the persistence side needs to resolve a dog list: the ownership filters already resolved to
 * concrete user ids by the service case, the text searches, and the page window translated to
 * {@code offset}/{@code limit}. A {@code null} {@code limit} means the whole list is fetched.
 * {@code nameContains} and {@code identificationContains} are matched as alternatives: a dog is listed
 * when either of the two provided searches matches it.
 */
public record DogListFilter(String owner, String creator, String nameContains, String country, Integer offset,
                            Integer limit, String identificationContains) {

    public static DogListFilter from(String owner, String creator, GetDogListCommand command) {
        String name = blankToNull(command.name());
        String country = blankToNull(command.country());
        String identification = blankToNull(command.identification());
        if (command.size() == null) {
            return new DogListFilter(owner, creator, name, country, null, null, identification);
        }
        int page = command.page() == null ? 0 : command.page();
        return new DogListFilter(owner, creator, name, country, page * command.size(), command.size(), identification);
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
