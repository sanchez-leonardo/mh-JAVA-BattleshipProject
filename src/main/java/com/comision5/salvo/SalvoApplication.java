package com.comision5.salvo;

import com.comision5.salvo.clases.*;
import com.comision5.salvo.restrepositories.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class SalvoApplication {

  public static void main(String[] args) {
    SpringApplication.run(SalvoApplication.class, args);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();

  }

  @Bean
  public CommandLineRunner initData(PlayerRepository playerRepo, GameRepository gameRepo,
                                    GamePlayerRepository gamePlayerRepo, ShipRepository shipsRepo,
                                    SalvoRepository salvoesRepo, ScoreRepository scoresRepo) {
    return (args) -> {

      //Creación de jugadores y carga al reposiorio
      Player player1 = new Player("j.bauer@ctu.gov", passwordEncoder().encode("24"));
      Player player2 = new Player("c.obrian@ctu.gov", passwordEncoder().encode("42"));
      Player player3 = new Player("kim_bauer@gmail.com", passwordEncoder().encode("kb"));
      Player player4 = new Player("t.almeida@ctu.gov", passwordEncoder().encode("mole"));

      List<Player> testPayers = Arrays.asList(player1, player2, player3, player4);

      playerRepo.saveAll(testPayers);

      //Creación de fecha patron
      LocalDateTime gameDate = LocalDateTime.now();
      //Creación de juegos defasados por una hora
      Game game1 = new Game(gameDate);
      Game game2 = new Game(gameDate.plusHours(1));
      Game game3 = new Game(gameDate.plusHours(2));
      Game game4 = new Game(gameDate.plusHours(3));
      Game game5 = new Game(gameDate.plusHours(4));
      Game game6 = new Game(gameDate.plusHours(5));
      Game game7 = new Game(gameDate.plusHours(6));
      Game game8 = new Game(gameDate.plusHours(7));

      game1.setGameState("over");
      game2.setGameState("over");
      game3.setGameState("over");
      game4.setGameState("over");
      game5.setGameState("salvo");
      game6.setGameState("waiting_p2");
      game7.setGameState("waiting_p2");
      game8.setGameState("salvo");

      //Guardado de juegos al repositorio
      List<Game> testGames = Arrays.asList(game1, game2, game3, game4, game5, game6, game7, game8);

      gameRepo.saveAll(testGames);

      //Carga de los elementos al repositorio gameplayer
      GamePlayer g1p1 = new GamePlayer(player1, game1);
      GamePlayer g1p2 = new GamePlayer(player2, game1);
      GamePlayer g2p1 = new GamePlayer(player1, game2);
      GamePlayer g2p2 = new GamePlayer(player2, game2);
      GamePlayer g3p2 = new GamePlayer(player2, game3);
      GamePlayer g3p4 = new GamePlayer(player4, game3);
      GamePlayer g4p2 = new GamePlayer(player2, game4);
      GamePlayer g4p1 = new GamePlayer(player1, game4);
      GamePlayer g5p4 = new GamePlayer(player4, game5);
      GamePlayer g5p1 = new GamePlayer(player1, game5);
      GamePlayer g6p3 = new GamePlayer(player3, game6);
      GamePlayer g7p4 = new GamePlayer(player4, game7);
      GamePlayer g8p3 = new GamePlayer(player3, game8);
      GamePlayer g8p4 = new GamePlayer(player4, game8);

      g1p1.setGamePlayerState("win");
      g1p2.setGamePlayerState("loss");
      g2p1.setGamePlayerState("tie");
      g2p2.setGamePlayerState("tie");
      g3p2.setGamePlayerState("win");
      g3p4.setGamePlayerState("loss");
      g4p2.setGamePlayerState("tie");
      g4p1.setGamePlayerState("tie");
      g5p4.setGamePlayerState("placing");
      g5p1.setGamePlayerState("placing");
      g6p3.setGamePlayerState("waiting");
      g7p4.setGamePlayerState("placing");
      g8p3.setGamePlayerState("placing");
      g8p4.setGamePlayerState("placing");

      List<GamePlayer> testGamePLayers = Arrays.asList(g1p1, g1p2, g2p1, g2p2, g3p2, g3p4, g4p2, g4p1, g5p4,
              g5p1, g6p3, g7p4, g8p3, g8p4);

      gamePlayerRepo.saveAll(testGamePLayers);

      //Creación de barcos
      Ship ship1P1G1 = new Ship("submarine", Arrays.asList("h2", "h3", "h4"), g1p1);
      Ship ship2P1G1 = new Ship("patrol_boat", Arrays.asList("b4", "b5"), g1p1);
      Ship ship1P2G1 = new Ship("destroyer", Arrays.asList("b5", "c5", "d5"), g1p2);
      Ship ship2P2G1 = new Ship("patrol_boat", Arrays.asList("f1", "f2"), g1p2);
      Ship ship1P1G2 = new Ship("destroyer", Arrays.asList("b5", "c5", "d5"), g2p1);
      Ship ship2P1G2 = new Ship("patrol_boat", Arrays.asList("c6", "c7"), g2p1);
      Ship ship1P2G2 = new Ship("submarine", Arrays.asList("a2", "a3", "a4"), g2p2);
      Ship ship2P2G2 = new Ship("patrol_boat", Arrays.asList("g6", "h6"), g2p2);
      Ship ship1P2G3 = new Ship("destroyer", Arrays.asList("b5", "c5", "d5"), g3p2);
      Ship ship2P2G3 = new Ship("patrol_boat", Arrays.asList("c6", "c7"), g3p2);
      Ship ship1P4G3 = new Ship("submarine", Arrays.asList("a2", "a3", "a4"), g3p4);
      Ship ship2P4G3 = new Ship("patrol_boat", Arrays.asList("g6", "h6"), g3p4);
      Ship ship1P2G4 = new Ship("destroyer", Arrays.asList("b5", "c5", "d5"), g4p2);
      Ship ship2P2G4 = new Ship("patrol_boat", Arrays.asList("c6", "c7"), g4p2);
      Ship ship1P1G4 = new Ship("submarine", Arrays.asList("a2", "a3", "a4"), g4p1);
      Ship ship2P1G4 = new Ship("patrol_boat", Arrays.asList("g6", "h6"), g4p1);
      Ship ship1P4G5 = new Ship("destroyer", Arrays.asList("b5", "c5", "d5"), g5p4);
      Ship ship2P4G5 = new Ship("patrol_boat", Arrays.asList("c6", "c7"), g5p4);
      Ship ship1P1G5 = new Ship("submarine", Arrays.asList("a2", "a3", "a4"), g5p1);
      Ship ship2P1G5 = new Ship("patrol_boat", Arrays.asList("g6", "h6"), g5p1);
      Ship ship1P3G6 = new Ship("destroyer", Arrays.asList("b5", "c5", "d5"), g6p3);
      Ship ship2P3G6 = new Ship("patrol_boat", Arrays.asList("c6", "c7"), g6p3);
      Ship ship1P3G8 = new Ship("destroyer", Arrays.asList("b5", "c5", "d5"), g8p3);
      Ship ship2P3G8 = new Ship("patrol_boat", Arrays.asList("c6", "c7"), g8p3);
      Ship ship1P4G8 = new Ship("submarine", Arrays.asList("a2", "a3", "a4"), g8p4);
      Ship ship2P4G8 = new Ship("patrol_boat", Arrays.asList("g6", "h6"), g8p4);

      List<Ship> testShips = Arrays.asList(ship1P1G1, ship2P1G1, ship1P2G1, ship2P2G1, ship1P1G2, ship2P1G2,
              ship1P2G2, ship2P2G2, ship1P2G3, ship2P2G3, ship1P4G3, ship2P4G3, ship1P2G4, ship2P2G4, ship1P1G4,
              ship2P1G4, ship1P4G5, ship2P4G5, ship1P1G5, ship2P1G5, ship1P3G6, ship2P3G6, ship1P3G8, ship2P3G8,
              ship1P4G8, ship2P4G8);

      shipsRepo.saveAll(testShips);

      //Creación de salvoes
      Salvo salvoP1G1T1 = new Salvo(1, Arrays.asList("b5", "c5", "f1"), g1p1);
      Salvo salvoP1G1T2 = new Salvo(2, Arrays.asList("f2", "d5"), g1p1);
      Salvo salvoP2G1T1 = new Salvo(1, Arrays.asList("b4", "b5", "b6"), g1p2);
      Salvo salvoP2G1T2 = new Salvo(2, Arrays.asList("e1", "h3", "a2"), g1p2);
      Salvo salvoP1G2T1 = new Salvo(1, Arrays.asList("a2", "a4", "g6"), g2p1);
      Salvo salvoP1G2T2 = new Salvo(2, Arrays.asList("a3", "h6"), g2p1);
      Salvo salvoP2G2T1 = new Salvo(1, Arrays.asList("b5", "d5", "c7"), g2p2);
      Salvo salvoP2G2T2 = new Salvo(2, Arrays.asList("c5", "c6"), g2p2);
      Salvo salvoP2G3T1 = new Salvo(1, Arrays.asList("g6", "h6", "a4"), g3p2);
      Salvo salvoP2G3T2 = new Salvo(2, Arrays.asList("a2", "a3", "d8"), g3p2);
      Salvo salvoP4G3T1 = new Salvo(1, Arrays.asList("h1", "h2", "h3"), g3p4);
      Salvo salvoP4G3T2 = new Salvo(2, Arrays.asList("e1", "f2", "g3"), g3p4);
      Salvo salvoP2G4T1 = new Salvo(1, Arrays.asList("a3", "a4", "f7"), g4p2);
      Salvo salvoP2G4T2 = new Salvo(2, Arrays.asList("a2", "g6", "h6"), g4p2);
      Salvo salvoP1G4T1 = new Salvo(1, Arrays.asList("b5", "c6", "h1"), g4p1);
      Salvo salvoP1G4T2 = new Salvo(2, Arrays.asList("c5", "c7", "d5"), g4p1);
      Salvo salvoP4G5T1 = new Salvo(1, Arrays.asList("a1", "a2", "a3"), g5p4);
      Salvo salvoP4G5T2 = new Salvo(2, Arrays.asList("g6", "g7", "g8"), g5p4);
      Salvo salvoP1G5T1 = new Salvo(1, Arrays.asList("b5", "b6", "c7"), g5p1);
      Salvo salvoP1G5T2 = new Salvo(2, Arrays.asList("c6", "d6", "e6"), g5p1);
      Salvo salvoP1G5T3 = new Salvo(3, Arrays.asList("h1", "h8"), g5p1);

      List<Salvo> testSalvoes = Arrays.asList(salvoP1G1T1, salvoP1G1T2, salvoP2G1T1, salvoP2G1T2, salvoP1G2T1,
              salvoP1G2T2, salvoP2G2T1, salvoP2G2T2, salvoP2G3T1, salvoP2G3T2, salvoP4G3T1, salvoP4G3T2,
              salvoP2G4T1, salvoP2G4T2, salvoP1G4T1, salvoP1G4T2, salvoP4G5T1, salvoP4G5T2, salvoP1G5T1,
              salvoP1G5T2, salvoP1G5T3);

      salvoesRepo.saveAll(testSalvoes);


      Score scoreP1G1 = new Score(player1, game1, 1);
      Score scoreP2G1 = new Score(player2, game1, 0);
      Score scoreP1G2 = new Score(player1, game2, 0.5);
      Score scoreP2G2 = new Score(player2, game2, 0.5);
      Score scoreP2G3 = new Score(player2, game3, 1);
      Score scoreP4G3 = new Score(player4, game3, 0);
      Score scoreP2G4 = new Score(player2, game4, 0.5);
      Score scoreP1G4 = new Score(player1, game4, 0.5);

      List<Score> testScores = Arrays.asList(scoreP1G1, scoreP2G1, scoreP1G2, scoreP2G2, scoreP2G3, scoreP4G3,
              scoreP2G4, scoreP1G4/*, scoreP3G8, scoreP4G8*/);

      scoresRepo.saveAll(testScores);

    };

  }

}


@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

  @Autowired
  PlayerRepository playerRepository;

  @Override
  public void init(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userName -> {
      Player player = playerRepository.findByUserName(userName);
      if (player != null) {
        return new User(player.getUserName(), player.getPassword(),
                AuthorityUtils.createAuthorityList("USER"));
      } else {
        throw new UsernameNotFoundException("Unknown user: " + userName);
      }
    });
  }

}

@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
            .antMatchers("/rest/**").denyAll()
            .antMatchers("/api/game_view/**",
                    "/api/game/**/players", "/api/games/players/**/ships").hasAuthority("USER")
            .antMatchers("/**").permitAll()
            .anyRequest().authenticated();

    http.formLogin()
            .usernameParameter("userName")
            .passwordParameter("password")
            .loginProcessingUrl("/api/login")
            .loginPage("/web/boarding_page.html")
            .permitAll();

    http.logout()
            .logoutUrl("/api/logout")
            .permitAll();

    //Cors
    http.cors();

    // turn off checking for CSRF tokens
    http.csrf().disable();

    // if user is not authenticated, just send an authentication failure response
    http.exceptionHandling()
            .authenticationEntryPoint(
                    (req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sin autentificación" + " " + exc));

    // if login is successful, just clear the flags asking for authentication
    http.formLogin()
            .successHandler(
                    (req, res, auth) -> clearAuthenticationAttributes(req));

    // if login fails, just send an authentication failure response
    http.formLogin()
            .failureHandler(
                    (req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Algo Falló" + " " + exc));

    // if logout is successful, just send a success response
    http.logout()
            .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    final CorsConfiguration configuration = new CorsConfiguration();
//    configuration.applyPermitDefaultValues();
    configuration.addAllowedOrigin("https://mh-battleshipgame.herokuapp.com");
    configuration.addAllowedMethod("*");
    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
    configuration.setAllowCredentials(true);
    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }


  private void clearAuthenticationAttributes(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }
  }

}
