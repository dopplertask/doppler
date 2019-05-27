package se.feraswilson.taskservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.feraswilson.taskservice.domain.action.Action;

@Repository
public interface ActionDao extends JpaRepository<Action, Long> {
}
