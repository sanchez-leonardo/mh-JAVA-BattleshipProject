package com.comision5.salvo.clases;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class GamePlayer {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
  @GenericGenerator(name = "native", strategy = "native")
  private long id;

  private LocalDateTime joinTime;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "player_id")
  private Player player;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "game_id")
  private Game game;

  @OneToMany(mappedBy = "gamePlayer")
  private List<Ship> ships;

  @OneToMany(mappedBy = "gamePlayer")
  private List<Salvo> salvoes;

  private String gamePlayerState;


  public GamePlayer() {
    this.joinTime = LocalDateTime.now();
  }

  public GamePlayer(Player player, Game game) {
    this.joinTime = LocalDateTime.now();
    this.player = player;
    this.game = game;
  }


  public long getId() {
    return id;
  }

  public LocalDateTime getJoinTime() {
    return joinTime;
  }

  @JsonIgnore
  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  @JsonIgnore
  public Game getGame() {
    return game;
  }

  public void setGame(Game game) {
    this.game = game;
  }

  @JsonIgnore
  public List<Ship> getShips() {
    return ships;
  }

  public void setShips(List<Ship> ships) {
    this.ships = ships;
  }

  public void addShip(Ship ship) {
    this.ships.add(ship);
  }

  public List<Salvo> getSalvoes() {
    return salvoes;
  }

  public void setSalvoes(List<Salvo> salvoes) {
    this.salvoes = salvoes;
  }

  public void addSalvo(Salvo salvo) {
    this.salvoes.add(salvo);
  }

  public String getGamePlayerState() {
    return gamePlayerState;
  }

  public void setGamePlayerState(String gamePlayerState) {
    this.gamePlayerState = gamePlayerState;
  }

  //Devuelve el score de este jugador en particular en el juego determinado
  public Score scoreByGame() {
    return getPlayer().getScores().stream()
            .filter(score -> score.getGame().equals(getGame()))
            .findFirst().orElse(null);

  }

  //Métodos para DTOs
  //Extrae Ids, creation y players para gameDTO en /games
  public Map<String, Object> gamePlayerDetail() {
    Map<String, Object> gamePlayerDetail = new LinkedHashMap<>();

    gamePlayerDetail.put("id", getId());
    gamePlayerDetail.put("player", getPlayer().playerDetail());
    gamePlayerDetail.put("game_player_state", getGamePlayerState());
    gamePlayerDetail.put("score", scoreByGame() == null ? null : scoreByGame().getScore());

    return gamePlayerDetail;
  }

  //Game player DTO para /game_view
  public Map<String, Object> gamePlayerDTO() {
    Map<String, Object> gamePlayerObj = new LinkedHashMap<>();

    gamePlayerObj.put("game_id", getGame().getId());
    gamePlayerObj.put("game_creation", getGame().getGameCreation().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
    gamePlayerObj.put("game_state", getGame().getGameState());
    gamePlayerObj.put("game_turn", getGame().getTurn());
    gamePlayerObj.put("game_player_state", getGamePlayerState());
    gamePlayerObj.put("game_players", getGame().gamePlayerArray());
    gamePlayerObj.put("ships", playerShipsArray());
    gamePlayerObj.put("salvoes", getGame().salvoesByPlayer());
    gamePlayerObj.put("fleet_status", getGame().fleetStatusReport());

    return gamePlayerObj;

  }

  //Extrae datos de game player para /game_view
  public Map<String, Object> gamePlayerObject() {

    Map<String, Object> gamePlayerObj = new LinkedHashMap<>();

    gamePlayerObj.put("game_player_id", getId());
    gamePlayerObj.put("game_player_join_date", getJoinTime().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
    gamePlayerObj.put("player_detail", getPlayer().playerDetail());

    return gamePlayerObj;
  }

  //Ship List para /game_view
  private List<Object> playerShipsArray() {
    return getShips().stream().map(Ship::shipDetails).collect(Collectors.toList());
  }

  //Salvoes per turn object para /game_view
  public Map<Integer, List<String>> salvoesByTurn() {
    return getSalvoes().stream().collect(Collectors.toMap(Salvo::getTurn, Salvo::getSalvoLocations));
  }

  //Estado de flota per game player para /game view
  //Lista de objetos, separado por player id -> turno -> barcos

  public List<Object> shipStatusPerTurn() {
    List<Object> shipStatus = new ArrayList<>();

    //Comprueba que haya 2 jugadores y que la lista de salvoes del oponente tenga salvoes
    //para no caer en nullpointerexception, debería ser corregido con la implementación de estados de partida
    if ((getGame().getGameState().equals("salvo") || getGame().getGameState().equals("over"))
            && getGame().getGamePlayers().stream()
            .filter(gp -> gp.getId() != this.getId()).findFirst().get().getSalvoes() != null) {

      //Lista de salvoes del oponente
      List<Salvo> opponentSalvoes = getGame().getGamePlayers().stream()
              .filter(gp -> gp.getId() != this.getId()).findFirst().get().getSalvoes();

      List<String> allSalvoLocations = new ArrayList<>();

      opponentSalvoes.forEach(salvo -> {
        //Acumula las ubicaciones analizadas en una lista,
        // de esa manera se puede dar el turno donde se hundió el barco
        //con una comprobación más adelante
        allSalvoLocations.addAll(salvo.getSalvoLocations());

        Map<String, Object> turnObj = new LinkedHashMap<>();

        turnObj.put("turn", salvo.getTurn());

        List<Object> hitList = getShips().stream()
                .filter(ship -> ship.wasHit(salvo.getSalvoLocations()) != null)
                .map(ship -> {

                  Map<String, Object> dmg = new LinkedHashMap<>();

                  dmg.put("ship", ship.getShipType());
                  dmg.put("dmg", ship.wasHit(salvo.getSalvoLocations()));
                  dmg.put("sunk", ship.wasSunk(allSalvoLocations));

                  return dmg;

                }).collect(Collectors.toList());

        turnObj.put("hits", hitList);

        shipStatus.add(turnObj);
      });

    }

    return shipStatus;

  }

  //STATE CHANGER v2
  //GamePlayer
  public void gamePlayerStateChanger() {

    switch (getGame().getGameState()) {

      case "waiting_p2":
      case "ship":
        if (getShips() != null && getShips().size() != 0) {
          setGamePlayerState("waiting");
        } else {
          setGamePlayerState("placing");
        }
        break;

      case "salvo":
        boolean gpTurnSalvo = getSalvoes().stream()
                .anyMatch(salvo -> salvo.getTurn() == getGame().getTurn());

        if (gpTurnSalvo) {
          setGamePlayerState("waiting");
        } else {
          setGamePlayerState("placing");
        }
        break;
    }

  }

}
