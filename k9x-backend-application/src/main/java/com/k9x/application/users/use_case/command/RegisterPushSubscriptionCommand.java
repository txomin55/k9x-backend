package com.k9x.application.users.use_case.command;

public record RegisterPushSubscriptionCommand(String endpoint, String auth, String p256dh) {
}
