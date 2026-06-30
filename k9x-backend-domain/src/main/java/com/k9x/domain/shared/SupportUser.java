package com.k9x.domain.shared;

/**
 * The platform support account. This user is a privileged superuser: every authorization gate
 * (organizer role, creator/owner ownership) and every lifecycle/deleted-state guard is bypassed for
 * it, so support can read and mutate any resource regardless of the state it is in. Existence guards
 * (e.g. "not found") are NOT bypassed — support still cannot operate on a resource that does not exist.
 */
public final class SupportUser {

    public static final String EMAIL = "k9x.support@gmail.com";

    private SupportUser() {}

    public static boolean is(String userId) {
        return EMAIL.equals(userId);
    }
}
