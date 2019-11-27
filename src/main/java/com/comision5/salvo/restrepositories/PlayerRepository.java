package com.comision5.salvo.restrepositories;

import com.comision5.salvo.clases.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface PlayerRepository extends JpaRepository<Player, Long> {

  Player findByUserName(@Param(value = "userName") String userName);

}
