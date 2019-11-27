package com.comision5.salvo.clases;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Entity
public class Player {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
  @GenericGenerator(name = "native", strategy = "native")
  private long id;

  private String userName;
  private String password;

  @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
  private List<GamePlayer> gamePlayers;

  @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
  private List<Score> scores;


  public Player() {
  }

  public Player(String userName, String password) {
    this.userName = userName;
    this.password = password;
    this.scores = new LinkedList<>();
  }


  public long getId() {
    return this.id;
  }

  public String getUserName() {
    return this.userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
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

  public void addScore(Score score) {
    this.scores.add(score);
  }


  //Métodos para DTOs
  //Extrae Player IDs y Username
  public Map<String, Object> playerDetail() {
    Map<String, Object> playerObj = new LinkedHashMap<>();

    playerObj.put("id", getId());
    playerObj.put("email", getUserName());

    return playerObj;
  }

  //Datos para /leaderboard
  public Map<String, Object> leaderBoardDTO() {
    Map<String, Object> playerObj = new LinkedHashMap<>();

    playerObj.put("id", getId());
    playerObj.put("email", getUserName());
    playerObj.put("scores", scoreBreakdown());

    return playerObj;
  }

  public Map<String, Object> scoreBreakdown() {
    Map<String, Object> scoresObj = new LinkedHashMap<>();

    scoresObj.put("matches", getScores().size());
    scoresObj.put("total", totalPoints());
    scoresObj.put("win", winLooseOrTie(1.0));
    scoresObj.put("loose", winLooseOrTie(0.0));
    scoresObj.put("tie", winLooseOrTie(0.5));

    return scoresObj;
  }

  private double totalPoints() {
    return getScores().stream().reduce(0.0, (a, b) -> a + b.getScore(), Double::sum);
  }

  private int winLooseOrTie(double value) {
    return getScores().stream().reduce(0, (a, b) -> a + countPattern(b.getScore(), value), Integer::sum);
  }

  private int countPattern(double score, double value) {
    return score == value ? 1 : 0;
  }

}
