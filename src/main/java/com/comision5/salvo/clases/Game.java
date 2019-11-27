package com.comision5.salvo.clases;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class Game {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
  @GenericGenerator(name = "native", strategy = "native")
  private long id;

  @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
  private List<GamePlayer> gamePlayers;

  @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
  private List<Score> scores;

  private LocalDateTime gameCreation;

  private String gameState;

  private int turn;


  //Constructor por defecto, solo genera una fecha automáticamente
  //Requerido por Spring y el único constructor necesario para la aplicación final
  public Game() {
    this.gameCreation = LocalDateTime.now();
  }

  //Constructor de producción y prueba, a ser borrado
  public Game(LocalDateTime gameTime) {
    this.gameCreation = gameTime;
  }


  public long getId() {
    return this.id;
  }

  public LocalDateTime getGameCreation() {
    return this.gameCreation;
  }

  public String getGameState() {
    return gameState;
  }

  public void setGameState(String gameState) {
    this.gameState = gameState;
  }

  public int getTurn() {
    return turn;
  }

  public void setTurn(int turn) {
    this.turn = turn;
  }

  public List<GamePlayer> getGamePlayers() {
    return gamePlayers;
  }

  public void setGamePlayers(List<GamePlayer> gamePlayers) {
    this.gamePlayers = gamePlayers;
  }

  public void addGamePlayer(GamePlayer gamePlayer) {
    this.gamePlayers.add(gamePlayer);
  }

  public List<Score> getScores() {
    return scores;
  }

  public void setScores(List<Score> scores) {
    this.scores = scores;
  }


  //Métodos para generar DTOs
  //Extraer la info de cada juego para /games
  public Map<String, Object> gameDTO() {
    Map<String, Object> gameDTO = new LinkedHashMap<>();

    gameDTO.put("id", getId());
    gameDTO.put("created", getGameCreation().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
    gameDTO.put("game_state", getGameState());
    gameDTO.put("game_players", getGamePlayers().stream().map(GamePlayer::gamePlayerDetail).collect(Collectors.toList()));

    return gameDTO;
  }

  //Game Player List para gamePlayerDTO en /game_view
  public List<Object> gamePlayerArray() {
    return getGamePlayers().stream().map(GamePlayer::gamePlayerObject).collect(Collectors.toList());

  }

  //Salvoes per player in a game object (si existen, sino devuelve obj vacío) para /game view
  public Map<Long, Object> salvoesByPlayer() {
    return getGamePlayers().stream().filter(gamePlayer ->
            gamePlayer.getSalvoes().size() != 0).collect(Collectors.toMap(gp -> gp.getPlayer().getId(),
            GamePlayer::salvoesByTurn, (s, a) -> s + ", " + a, LinkedHashMap::new));
  }

  //fleet status para /game_view
  public Map<Long, Object> fleetStatusReport() {
    return getGamePlayers().stream().collect(Collectors.toMap(gp -> gp.getPlayer().getId(), GamePlayer::shipStatusPerTurn));
  }

  //STATE CHANGER v2
  //Game
  public void gameStateChanger() {

    switch (getGamePlayers().size()) {

      case 1:
        setGameState("waiting_p2");
        break;

      case 2:
        List<Long> gamePlayerIdsWhoPlacedShips = getGamePlayers().stream()
                .filter(gp -> gp.getShips() != null && gp.getShips().size() != 0)
                .map(GamePlayer::getId)
                .collect(Collectors.toList());

        if (gamePlayerIdsWhoPlacedShips.size() == 2) {
          setGameState("salvo");

          if (getTurn() == 0) {
            setTurn(1);
          }

        } else {
          setGameState("ship");
        }
        break;
    }

  }

  //GamePlayers
  public void gamePlayerUpdater() {
    getGamePlayers().forEach(GamePlayer::gamePlayerStateChanger);
  }

  //Turn Advancement
  public void turnAdvancer(Long gpId) {

    GamePlayer gamePlayerP1 = getGamePlayers().stream().filter(gp -> gp.getId() == gpId).findFirst().get();

    GamePlayer gamePlayerP2 = getGamePlayers().stream().filter(gp -> gp.getId() != gpId).findFirst().get();

    boolean p1TurnSalvo = gamePlayerP1.getSalvoes().stream().anyMatch(salvo -> salvo.getTurn() == this.getTurn());

    boolean p2TurnSalvo = gamePlayerP2.getSalvoes().stream().anyMatch(salvo -> salvo.getTurn() == this.getTurn());

    if (p1TurnSalvo && p2TurnSalvo) {

      List<String> p1ShipLocations = gamePlayerP1.getShips().stream()
              .flatMap(ship -> ship.getShipLocations().stream())
              .collect(Collectors.toList());

      List<String> p1SalvoesLocations = gamePlayerP1.getSalvoes().stream()
              .flatMap(salvo -> salvo.getSalvoLocations().stream()).collect(Collectors.toList());

      List<String> p2ShipLocations = gamePlayerP2.getShips().stream()
              .flatMap(ship -> ship.getShipLocations().stream())
              .collect(Collectors.toList());

      List<String> p2SalvoesLocations = gamePlayerP2.getSalvoes().stream()
              .flatMap(salvo -> salvo.getSalvoLocations().stream())
              .collect(Collectors.toList());

      if (p1SalvoesLocations.containsAll(p2ShipLocations) && p2SalvoesLocations.containsAll(p1ShipLocations)) {
        setGameState("over");
        gamePlayerP1.setGamePlayerState("tie");
        gamePlayerP2.setGamePlayerState("tie");

      } else if (p1SalvoesLocations.containsAll(p2ShipLocations)) {
        setGameState("over");
        gamePlayerP1.setGamePlayerState("win");
        gamePlayerP2.setGamePlayerState("loss");

      } else if (p2SalvoesLocations.containsAll(p1ShipLocations)) {
        setGameState("over");
        gamePlayerP1.setGamePlayerState("loss");
        gamePlayerP2.setGamePlayerState("win");

      } else {
        setTurn(getTurn() + 1);
        getGamePlayers().forEach(gp -> gp.setGamePlayerState("placing"));

      }

    }
  }

  public void scoreSetter() {

    if (getGameState().equals("over")) {
      List<Score> gameScores = new LinkedList<>();

      getGamePlayers().forEach(gp -> {
        switch (gp.getGamePlayerState()) {

          case "win":
            Score winScore = new Score(gp.getPlayer(), gp.getGame(), 1);
            gp.getPlayer().addScore(winScore);
            gameScores.add(winScore);
            break;

          case "loss":
            Score lossScore = new Score(gp.getPlayer(), gp.getGame(), 0);
            gp.getPlayer().addScore(lossScore);
            gameScores.add(lossScore);
            break;

          case "tie":
            Score tieScore = new Score(gp.getPlayer(), gp.getGame(), 0.5);
            gp.getPlayer().addScore(tieScore);
            gameScores.add(tieScore);
            break;

        }
      });

      this.setScores(gameScores);

    }
  }


}
