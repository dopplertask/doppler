package se.feraswilson.automationservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.feraswilson.automationservice.domain.Task;

@Repository
public interface TaskDao extends JpaRepository<Task, Long> {
}
