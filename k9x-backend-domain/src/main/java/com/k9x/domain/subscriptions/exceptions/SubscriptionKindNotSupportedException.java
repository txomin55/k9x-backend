package com.k9x.domain.subscriptions.exceptions;

import com.k9x.domain.exceptions.DomainException;
import com.k9x.domain.exceptions.error.ErrorEnum;

public class SubscriptionKindNotSupportedException extends DomainException {

    public SubscriptionKindNotSupportedException(String kind) {
        super(ErrorEnum.SUBSCRIPTION_KIND_NOT_SUPPORTED, new String[]{kind});
    }
}
