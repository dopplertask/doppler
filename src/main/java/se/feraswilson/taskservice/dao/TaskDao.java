package se.feraswilson.taskservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.feraswilson.taskservice.domain.Task;

@Repository
public interface TaskDao extends JpaRepository<Task, Long> {
}
