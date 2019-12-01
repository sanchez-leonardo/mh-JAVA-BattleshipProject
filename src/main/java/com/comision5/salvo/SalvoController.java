package com.comision5.salvo;

import com.comision5.salvo.clases.*;
import com.comision5.salvo.restrepositories.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "https://mh-battleshipgame.herokuapp.com", allowedHeaders = {"Access-Control-Allow-Origins"})
@RestController
@RequestMapping("/api")
public class SalvoController {

  @Autowired
  private GameRepository gameRepo;

  @Autowired
  private PlayerRepository playerRepo;

  @Autowired
  private GamePlayerRepository gamePlayerRepo;

  @Autowired
  private ShipRepository shipRepo;

  @Autowired
  private SalvoRepository salvoRepo;

  @Autowired
  private ScoreRepository scoreRepo;

  @Autowired
  private PasswordEncoder passwordEncoder;

  // Llamado para crear la tabla de puntajes
  @GetMapping("/leaderboard")
  public List<Object> lederboardInfo() {
    return playerRepo.findAll().stream().map(Player::leaderBoardDTO).collect(Collectors.toList());
  }

  // Info general de todos los juegos
  @GetMapping("/games")
  public Map<String, Object> gamesInfo(Authentication auth) {

    Map<String, Object> gamesInfoDTO = new LinkedHashMap<>();

    gamesInfoDTO.put("currentUser", isGuest(auth) ? null : playerRepo.findByUserName(auth.getName()).playerDetail());
    gamesInfoDTO.put("games", gameRepo.findAll().stream().map(Game::gameDTO).collect(Collectors.toList()));

    return gamesInfoDTO;
  }

  // Método de estado de juego
  // Simplificaría pedidos de game view según el estado
  @GetMapping("/game_state/{gpId}")
  public Map<String, Object> getGameState(@PathVariable long gpId) {

    GamePlayer gamePlayer = gamePlayerRepo.findById(gpId).get();

    Map<String, Object> gameStateDTO = new LinkedHashMap<>();

    gameStateDTO.put("game_state", gamePlayer.getGame().getGameState());
    gameStateDTO.put("turn", gamePlayer.getGame().getTurn());
    gameStateDTO.put("game_player_state", gamePlayer.getGamePlayerState());

    return gameStateDTO;
  }

  // Game player específico
  @GetMapping("/game_view/{gpId}")
  public ResponseEntity<Object> gameView(@PathVariable long gpId, Authentication auth) {

    if (isGuest(auth)) {

      return new ResponseEntity<>("you are not a user", HttpStatus.UNAUTHORIZED);

    } else if (!gamePlayerExists(gpId)) {

      return new ResponseEntity<>("no such game", HttpStatus.NOT_FOUND);

    } else if (!samePlayer(gpId, auth)) {

      return new ResponseEntity<>("you can not see that", HttpStatus.FORBIDDEN);

    } else {

      GamePlayer gamePlayerPOV = gamePlayerRepo.findById(gpId).get();

      return new ResponseEntity<>(gamePlayerPOV.gamePlayerDTO(), HttpStatus.ACCEPTED);

    }
  }

  // Registro de usuario
  @PostMapping(path = "/players")
  public ResponseEntity<Object> register(@RequestParam String userName, @RequestParam String password) {

    if (userName.isEmpty() || password.isEmpty()) {

      return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);

    } else if (playerRepo.findByUserName(userName) != null) {

      return new ResponseEntity<>("Email already in use", HttpStatus.FORBIDDEN);

    } else {

      playerRepo.save(new Player(userName, passwordEncoder.encode(password)));
      return new ResponseEntity<>(HttpStatus.CREATED);

    }
  }

  // Creación de juego
  @PostMapping(path = "/games")
  public ResponseEntity<Object> createGame(Authentication auth) {

    if (isGuest(auth)) {

      return new ResponseEntity<>("you are not a user", HttpStatus.FORBIDDEN);

    } else {

      Game newGame = new Game();

      Player thePlayer = playerRepo.findByUserName(auth.getName());

      GamePlayer aNewGP = new GamePlayer(thePlayer, newGame);
      newGame.setGamePlayers(Collections.singletonList(aNewGP));
      thePlayer.addGamePlayer(aNewGP);
      // cambio de estado
      newGame.gameStateChanger();
      aNewGP.gamePlayerStateChanger();

      // Actualización y guardado de entidades
      gameRepo.save(newGame);

      gamePlayerRepo.save(aNewGP);

      playerRepo.save(thePlayer);

      return new ResponseEntity<>(responseObject("gpId", aNewGP.getId()), HttpStatus.CREATED);
    }

  }

  // Entrada a juego existente
  @PostMapping(path = "/game/{gameId}/players")
  public ResponseEntity<Object> joinGame(@PathVariable Long gameId, Authentication auth) {

    Player thePlayer = playerRepo.findByUserName(auth.getName());

    if (isGuest(auth)) {

      return new ResponseEntity<>("greetings, program", HttpStatus.UNAUTHORIZED);

    } else if (!optGame(gameId).isPresent()) {

      return new ResponseEntity<>("this is not the game you are looking for", HttpStatus.FORBIDDEN);

    } else if (optGame(gameId).get().getGamePlayers().size() > 1) {

      return new ResponseEntity<>("place is crowded", HttpStatus.FORBIDDEN);

    } else if (isAlreadyPlayer(optGame(gameId).get(), thePlayer)) {

      return new ResponseEntity<>("playing with yourself", HttpStatus.FORBIDDEN);

    } else {

      Game existingGame = gameRepo.findById(gameId).get();

      GamePlayer aNewGP = new GamePlayer(thePlayer, existingGame);

      existingGame.addGamePlayer(aNewGP);

      thePlayer.addGamePlayer(aNewGP);

      // Cambio de estado
      existingGame.gameStateChanger();
      aNewGP.gamePlayerStateChanger();

      // Actualización y guardado de entidades
      gameRepo.save(existingGame);

      gamePlayerRepo.save(aNewGP);

      playerRepo.save(thePlayer);

      return new ResponseEntity<>(responseObject("gpId", aNewGP.getId()), HttpStatus.CREATED);

    }

  }

  // Creación de lista de barcos de gamePlayer
  @PostMapping(path = "/games/players/{gpId}/ships")
  public ResponseEntity<Object> setGamePlayerShips(@PathVariable Long gpId, @RequestBody List<Ship> ships,
      Authentication auth) {

    if (isGuest(auth)) {

      return new ResponseEntity<>("greetings, program", HttpStatus.UNAUTHORIZED);

    } else if (!gamePlayerExists(gpId)) {

      return new ResponseEntity<>("no such game", HttpStatus.UNAUTHORIZED);

    } else if (!samePlayer(gpId, auth)) {

      return new ResponseEntity<>("ye can't sail yer ships thar", HttpStatus.UNAUTHORIZED);

    } else if (ships.size() != 5) {

      return new ResponseEntity<>("incorrect amount", HttpStatus.FORBIDDEN);

    } else if (gamePlayerRepo.findById(gpId).get().getShips().size() == 5) {

      return new ResponseEntity<>("no no no, no more ships for ya", HttpStatus.FORBIDDEN);

    } else {

      List<Ship> shipList = new ArrayList<>();

      GamePlayer gamePlayer = gamePlayerRepo.findById(gpId).get();

      Game theGame = gameRepo.findById(gamePlayer.getGame().getId()).get();

      ships.forEach(vessel -> {

        Ship newShip = new Ship(vessel.getShipType(), vessel.getShipLocations(), gamePlayer);

        shipList.add(newShip);

      });

      gamePlayer.setShips(shipList);

      // Actualización de estados
      theGame.gameStateChanger();
      theGame.gamePlayerUpdater();

      // Actualización y guardado de entidades
      gameRepo.save(theGame);

      shipRepo.saveAll(shipList);

      gamePlayerRepo.save(gamePlayer);

      return new ResponseEntity<>("ships placed", HttpStatus.CREATED);

    }
  }

  // Creación de lista de salvoes de gamePlayer
  @PostMapping(path = "/games/players/{gpId}/salvoes")
  public ResponseEntity<Object> setGamePlayerSalvoes(@PathVariable Long gpId, @RequestBody List<String> salvoes,
      Authentication auth) {

    if (isGuest(auth)) {

      return new ResponseEntity<>("greetings, program", HttpStatus.UNAUTHORIZED);

    } else if (!gamePlayerExists(gpId)) {

      return new ResponseEntity<>("no such game", HttpStatus.FORBIDDEN);

    } else if (!samePlayer(gpId, auth)) {

      return new ResponseEntity<>("keep yer cannonballs to yerself", HttpStatus.UNAUTHORIZED);

    } else if (salvoes.size() > 5) {

      return new ResponseEntity<>("too many shots", HttpStatus.FORBIDDEN);

    } else {
      // Usa el estado de juego y game player para setear los salvos
      GamePlayer gamePlayerP1 = gamePlayerRepo.findById(gpId).get();

      Game theGame = gameRepo.findById(gamePlayerP1.getGame().getId()).get();

      if (theGame.getGameState().equals("salvo") && gamePlayerP1.getGamePlayerState().equals("placing")) {

        Salvo newSalvo = new Salvo(theGame.getTurn(), salvoes, gamePlayerRepo.findById(gpId).get());

        gamePlayerP1.addSalvo(newSalvo);

        // Actualización de estado de game players
        theGame.gamePlayerUpdater();

        theGame.turnAdvancer(gpId);

        theGame.scoreSetter();

        // Actualización y guardado de entidades
        salvoRepo.save(newSalvo);

        gamePlayerRepo.save(gamePlayerP1);

        if (theGame.getGameState().equals("over")) {

          scoreRepo.saveAll(theGame.getScores());

          playerRepo.save(gamePlayerP1.getPlayer());

        }

        gameRepo.save(theGame);

        return new ResponseEntity<>("salvoes placed", HttpStatus.CREATED);

      } else {

        return new ResponseEntity<>("You cannot shoot yet", HttpStatus.FORBIDDEN);

      }
    }

  }

  // AUX
  // Retorna true si no es usuario autentificado o es annonymous
  private boolean isGuest(Authentication auth) {
    return auth == null || auth instanceof AnonymousAuthenticationToken;
  }

  // Boolean si el player id es igual al id del player en el gameplayer pedido
  private boolean samePlayer(Long id, Authentication auth) {
    return gamePlayerRepo.findById(id).get().getPlayer().getId() == playerRepo.findByUserName(auth.getName()).getId();
  }

  // Boolean para comprobar que un game no tiene al jugador que quiere unirse
  private boolean isAlreadyPlayer(Game game, Player player) {

    return game.getGamePlayers().stream().map(gp -> gp.getPlayer().getId()).collect(Collectors.toList())
        .contains(player.getId());

  }

  // Boolean para comprobar si existe el game player
  private boolean gamePlayerExists(Long id) {
    return gamePlayerRepo.findById(id).isPresent();
  }

  // Retorna un objeto simple de 1 campo
  private Map<String, Object> responseObject(String key, Object value) {

    Map<String, Object> responseObj = new LinkedHashMap<>();

    responseObj.put(key, value);

    return responseObj;
  }

  // Retorna el optional de game. Es para simplificar comparaciones
  private Optional<Game> optGame(Long id) {
    return gameRepo.findById(id);
  }

}