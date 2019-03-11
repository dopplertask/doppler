package se.feraswilson.automationservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.feraswilson.automationservice.domain.TaskExecution;

@Repository
public interface TaskExecutionDao extends JpaRepository<TaskExecution, Long> {
}
