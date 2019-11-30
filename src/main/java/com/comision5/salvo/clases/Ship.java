package com.comision5.salvo.clases;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

@Entity
public class Ship {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
  @GenericGenerator(name = "native", strategy = "native")
  private long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "gamePlayer_id")
  private GamePlayer gamePlayer;

  private String shipType;

  @ElementCollection
  @Column(name = "ship_locations")
  private List<String> shipLocations;

  public Ship(String shipType, List<String> shipLocations, GamePlayer gamePlayer) {
    this.shipType = shipType;
    this.shipLocations = shipLocations;
    this.gamePlayer = gamePlayer;
  }

  public long getId() {
    return id;
  }

  public String getShipType() {
    return shipType;
  }

  public void setShipType(String shipType) {
    this.shipType = shipType;
  }

  public List<String> getShipLocations() {
    return shipLocations;
  }

  public void setShipLocations(List<String> shipLocations) {
    this.shipLocations = shipLocations;
  }

  public GamePlayer getGamePlayer() {
    return gamePlayer;
  }

  public void setGamePlayer(GamePlayer gamePlayer) {
    this.gamePlayer = gamePlayer;
  }

  // Métodos para DTOs
  // Objeto de detalles para /game_view
  public Map<String, Object> shipDetails() {
    Map<String, Object> shipObj = new LinkedHashMap<>();

    shipObj.put("type", getShipType());
    shipObj.put("locations", getShipLocations());

    return shipObj;
  }

  // comprueba si hubo algun hit en algun barco
  // Si los hay devuelve el array de impactos y si no los hay devuelve null
  // Sirve para comprobar y para dar resultados
  public List<String> wasHit(List<String> salvoes) {
    List<String> hitList = new ArrayList<>();

    if (getShipLocations().stream().anyMatch(salvoes::contains)) {
      getShipLocations().forEach(loc -> salvoes.forEach(salvo -> {
        if (salvo.equals(loc)) {
          hitList.add(loc);
        }
      }));
      return hitList;

    } else {
      return null;
    }

  }

  // Comprueba si el barco fue hundido
  // en base a una lista de ubicaciones provista

  public boolean wasSunk(List<String> salvoes) {
    return salvoes.containsAll(getShipLocations());
  }
}
