package com.dopplertask.doppler.dao;

import com.dopplertask.doppler.domain.action.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionDao extends JpaRepository<Action, Long> {
}
