package com.k9x.infrastructure.out.push;

import com.k9x.application.notifications.port.PushNotifier;
import com.k9x.application.notifications.port.SaveNotificationPersistencePort;
import com.k9x.application.notifications.port.SendPushNotificationPort;
import com.k9x.application.notifications.port.payload.SaveNotificationPersistencePayload;
import com.k9x.application.notifications.valueobjects.PushDeliveryStatus;
import com.k9x.application.notifications.valueobjects.PushNotification;
import com.k9x.application.users.port.DeletePushSubscriptionPersistencePort;
import com.k9x.application.users.port.GetPushSubscriptionsPersistencePort;
import com.k9x.application.users.use_case.dto.PushSubscriptionTargetDTO;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Delivers push notifications off the caller's thread: both entry points return immediately and the
 * fetch/send/prune work runs on a small daemon pool. This lets a transactional service case fire a
 * notification without holding its database connection open for the HTTP round-trip or delaying its
 * response, and guarantees a delivery failure can never roll back the originating transaction.
 *
 * <p>When called inside a transaction the delivery is deferred until <em>after commit</em>, so a push
 * is never sent for an operation that rolls back. Outside a transaction it dispatches immediately.
 *
 * <p>{@link #notify} also writes the inbox row, which therefore lands outside the caller's transaction
 * and best-effort. {@link #deliver} skips that write, for callers that persist the inbox row themselves
 * inside their transaction and need it to be guaranteed rather than best-effort.
 *
 * <p>Recipient selection and business rules (e.g. not notifying yourself) live in the calling service
 * case; this class only resolves the recipient's subscriptions, sends, and prunes expired ones.
 */
public class AsyncPushNotifier implements PushNotifier {

    private static final Logger log = System.getLogger(AsyncPushNotifier.class.getName());

    private final GetPushSubscriptionsPersistencePort getPushSubscriptionsPersistencePort;
    private final DeletePushSubscriptionPersistencePort deletePushSubscriptionPersistencePort;
    private final SendPushNotificationPort sendPushNotificationPort;
    private final SaveNotificationPersistencePort saveNotificationPersistencePort;
    private final ExecutorService executor;

    public AsyncPushNotifier(GetPushSubscriptionsPersistencePort getPushSubscriptionsPersistencePort,
                             DeletePushSubscriptionPersistencePort deletePushSubscriptionPersistencePort,
                             SendPushNotificationPort sendPushNotificationPort,
                             SaveNotificationPersistencePort saveNotificationPersistencePort) {
        this.getPushSubscriptionsPersistencePort = getPushSubscriptionsPersistencePort;
        this.deletePushSubscriptionPersistencePort = deletePushSubscriptionPersistencePort;
        this.sendPushNotificationPort = sendPushNotificationPort;
        this.saveNotificationPersistencePort = saveNotificationPersistencePort;
        this.executor = Executors.newFixedThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable, "push-notifier");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void notify(String recipientUserId, PushNotification notification) {
        dispatch(() -> recordAndSend(recipientUserId, notification));
    }

    @Override
    public void deliver(String recipientUserId, PushNotification notification) {
        dispatch(() -> send(recipientUserId, notification));
    }

    private void dispatch(Runnable delivery) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    executor.submit(delivery);
                }
            });
            return;
        }
        executor.submit(delivery);
    }

    private void recordAndSend(String recipientUserId, PushNotification notification) {
        try {
            // Persist the notification unconditionally so it is always readable in the user's inbox,
            // even when they have no push subscriptions and no push is actually delivered.
            saveNotificationPersistencePort.save(SaveNotificationPersistencePayload.from(recipientUserId, notification));
        } catch (Exception e) {
            log.log(Level.ERROR, "Recording notification for " + recipientUserId + " failed", e);
            return;
        }
        send(recipientUserId, notification);
    }

    private void send(String recipientUserId, PushNotification notification) {
        try {
            List<PushSubscriptionTargetDTO> subscriptions = getPushSubscriptionsPersistencePort.getByUserId(recipientUserId);
            if (subscriptions.isEmpty()) {
                log.log(Level.INFO, "No push subscriptions for {0}", recipientUserId);
                return;
            }
            log.log(Level.INFO, "Sending {0} push to {1} subscription(s) of {2}",
                    notification.type(), subscriptions.size(), recipientUserId);
            subscriptions.forEach(subscription -> {
                PushDeliveryStatus status = sendPushNotificationPort.send(subscription, notification);
                if (status == PushDeliveryStatus.EXPIRED) {
                    deletePushSubscriptionPersistencePort.deleteByEndpoint(subscription.endpoint());
                }
            });
        } catch (Exception e) {
            log.log(Level.ERROR, "Push delivery to " + recipientUserId + " failed", e);
        }
    }
}
