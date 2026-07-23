package com.k9x.application.users.use_case.dto;

public record PushSubscriptionTargetDTO(String endpoint, String p256dh, String auth) {
}
