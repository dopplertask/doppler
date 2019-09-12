package com.dopplertask.doppler.service;

import com.dopplertask.doppler.dao.TaskExecutionDao;
import com.dopplertask.doppler.domain.TaskExecution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExecutionServiceImpl implements ExecutionService {

    @Autowired
    private TaskExecutionDao taskExecutionDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveExecution(TaskExecution execution) {
        taskExecutionDao.save(execution);
    }
}
