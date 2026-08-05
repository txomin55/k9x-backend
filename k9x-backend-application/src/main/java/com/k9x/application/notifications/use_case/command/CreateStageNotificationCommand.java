package com.k9x.application.notifications.use_case.command;

import java.util.List;

public record CreateStageNotificationCommand(List<String> eventIds, String content) {
}
