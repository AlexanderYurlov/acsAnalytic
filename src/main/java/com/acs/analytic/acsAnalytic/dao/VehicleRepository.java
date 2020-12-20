package com.acs.analytic.acsAnalytic.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acs.analytic.acsAnalytic.model.vehicle.Vehicle;


public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
}
