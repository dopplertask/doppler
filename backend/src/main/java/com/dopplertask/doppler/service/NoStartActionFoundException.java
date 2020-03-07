package com.dopplertask.doppler.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoStartActionFoundException extends RuntimeException {
    public NoStartActionFoundException(String s) {
        super(s);
    }
}
