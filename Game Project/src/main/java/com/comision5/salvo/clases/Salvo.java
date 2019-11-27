package com.comision5.salvo.clases;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;

@Entity
public class Salvo {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
  @GenericGenerator(name = "native", strategy = "native")
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "gamePlayer_id")
  private GamePlayer gamePlayer;

  private int turn;

  @ElementCollection
  @Column(name = "salvo_locations")
  private List<String> salvoLocations;


  public Salvo() {
  }

  public Salvo(int turn, List<String> salvoLocations, GamePlayer gamePlayer) {
    this.turn = turn;
    this.salvoLocations = salvoLocations;
    this.gamePlayer = gamePlayer;
  }


  public GamePlayer getGamePlayer() {
    return gamePlayer;
  }

  public void setGamePlayer(GamePlayer gamePlayer) {
    this.gamePlayer = gamePlayer;
  }

  public int getTurn() {
    return turn;
  }

  public void setTurn(int turn) {
    this.turn = turn;
  }

  public List<String> getSalvoLocations() {
    return salvoLocations;
  }

  public void setSalvoLocations(List<String> salvoLocations) {
    this.salvoLocations = salvoLocations;
  }

}
