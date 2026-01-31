package com.k9x.infrastructure.in.rest.configuration.exception.error;

public record CustomError(
        String id,
        String msg,
        Long date,
        Integer code
) {
    public String getId() {
        return id;
    }

    public String getMsg() {
        return msg;
    }

    public Long getDate() {
        return date;
    }

    public Integer getCode() {
        return code;
    }
}
