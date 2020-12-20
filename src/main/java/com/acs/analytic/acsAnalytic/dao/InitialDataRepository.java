package com.acs.analytic.acsAnalytic.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acs.analytic.acsAnalytic.model.InitialData;


public interface InitialDataRepository extends JpaRepository<InitialData, Long> {
}
