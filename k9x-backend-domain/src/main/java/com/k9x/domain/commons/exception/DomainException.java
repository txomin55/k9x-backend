package com.k9x.domain.commons.exception;

import com.k9x.domain.commons.exception.error.ErrorEnum;

import java.io.Serial;
import java.util.Date;

public class DomainException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8093367381483867991L;

    private final String id;
    private final Long date;
    private final String[] args;

    public DomainException(ErrorEnum error) {
        super(error.getId());
        this.id = error.getId();
        this.date = new Date().getTime();
        this.args = null;
    }

    public DomainException(ErrorEnum error, String[] args) {
        super(error.getId());
        this.id = error.getId();
        this.date = new Date().getTime();
        this.args = args;
    }

    public String getId() {
        return id;
    }

    public Long getDate() {
        return date;
    }

    public String[] getArgs() {
        return args;
    }
}
