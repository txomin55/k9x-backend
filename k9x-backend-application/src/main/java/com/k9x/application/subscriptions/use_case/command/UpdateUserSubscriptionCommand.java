package com.k9x.application.subscriptions.use_case.command;

import java.util.List;

/**
 * Toggle request for a set of subscriptions of the same kind: {@code kind} + {@code ids} identify the
 * resources, {@code subscribe} states the desired end state (true = subscribed, false = not subscribed).
 * A whole set travels together because the UI toggles all the events of a stage with a single tap.
 */
public record UpdateUserSubscriptionCommand(String kind, List<String> ids, boolean subscribe) {
}
