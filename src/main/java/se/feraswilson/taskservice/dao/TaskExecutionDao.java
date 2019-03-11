package se.feraswilson.taskservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.feraswilson.taskservice.domain.TaskExecution;

@Repository
public interface TaskExecutionDao extends JpaRepository<TaskExecution, Long> {
}
