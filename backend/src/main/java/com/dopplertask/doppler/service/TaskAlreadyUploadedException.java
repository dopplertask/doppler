package com.dopplertask.doppler.service;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "This task is already uploaded.")
public class TaskAlreadyUploadedException extends RuntimeException {
    public TaskAlreadyUploadedException(String s) {
        super(s);
    }
}
