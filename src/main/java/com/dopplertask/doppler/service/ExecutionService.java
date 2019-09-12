package com.dopplertask.doppler.service;

import com.dopplertask.doppler.domain.TaskExecution;

public interface ExecutionService {
    void saveExecution(TaskExecution execution);
}
