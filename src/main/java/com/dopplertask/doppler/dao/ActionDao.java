package com.dopplertask.doppler.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dopplertask.doppler.domain.action.Action;

@Repository
public interface ActionDao extends JpaRepository<Action, Long> {
}
