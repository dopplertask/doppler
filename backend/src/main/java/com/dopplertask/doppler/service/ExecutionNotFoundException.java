package com.dopplertask.doppler.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Execution could not be found.")
public class ExecutionNotFoundException extends RuntimeException {
    public ExecutionNotFoundException(String s) {
        super(s);
    }
}
