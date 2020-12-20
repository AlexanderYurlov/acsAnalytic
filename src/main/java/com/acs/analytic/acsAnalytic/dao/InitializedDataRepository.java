package com.acs.analytic.acsAnalytic.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acs.analytic.acsAnalytic.model.InitializedData;


public interface InitializedDataRepository extends JpaRepository<InitializedData, Long> {
}
