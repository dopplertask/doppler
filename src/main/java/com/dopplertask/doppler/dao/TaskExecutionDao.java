package com.dopplertask.doppler.dao;

import com.dopplertask.doppler.domain.TaskExecution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskExecutionDao extends JpaRepository<TaskExecution, Long> {
}
