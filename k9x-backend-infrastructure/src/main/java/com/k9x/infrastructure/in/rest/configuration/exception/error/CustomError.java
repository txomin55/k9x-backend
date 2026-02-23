package com.k9x.infrastructure.in.rest.configuration.exception.error;

public record CustomError(
        String msg,
        Integer code
) {
    public String getMsg() {
        return msg;
    }

    public Integer getCode() {
        return code;
    }
}
