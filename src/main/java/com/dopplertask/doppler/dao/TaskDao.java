package com.dopplertask.doppler.dao;

import com.dopplertask.doppler.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskDao extends JpaRepository<Task, Long> {
    Optional<Task> findFirstByNameOrderByCreatedDesc(String taskName);

    Optional<Task> findByChecksum(String checksum);
}
