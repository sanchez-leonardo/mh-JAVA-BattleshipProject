package com.comision5.salvo.restrepositories;

import com.comision5.salvo.clases.Ship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface ShipRepository extends JpaRepository<Ship, Long> {
}
