import {
  SET_GAME_STATE_INFO,
  SET_GAME_VIEW_INFO,
  CLEAR_GAME_VIEW_INFO
} from "../mutationTypes";

const state = {
  gameView: {
    game_players: [],
    ships: [],
    salvoes: {}
  },
  gameState: {}
};

const getters = {
  gamePlayers: ({ gameView }) => gameView.game_players,

  //Id del oponente
  opponentId: ({ gameView }, getters) => {
    if (gameView.game_players.length === 2) {
      return gameView.game_players
        .flatMap(gp => gp.player_detail.id)
        .filter(id => id !== getters.currentUser.id)[0];
    } else {
      return false;
    }
  },

  //State from game player
  gameViewState: ({ gameView }) => {
    return {
      game_state: gameView.game_state,
      game_turn: gameView.game_turn,
      game_player_state: gameView.game_player_state
    };
  },

  //Only player ships
  playerShips: ({ gameView }) =>
    gameView.ships.lenght !== 0 ? gameView.ships : [],

  //Only player salvoes
  playerSalvoesLocations: ({ gameView }, getters) => {
    if (
      gameView.game_players.length == 2 &&
      !(
        Object.entries(gameView.salvoes).length === 0 &&
        gameView.salvoes.constructor === Object
      )
    ) {
      if (gameView.salvoes[getters.currentUser.id]) {
        return Object.values(gameView.salvoes[getters.currentUser.id]).flatMap(
          salvo => salvo
        );
      } else {
        return [];
      }
    } else {
      return [];
    }
  },

  //Oponent salvoes
  opponentSalvoesLocations: ({ gameView }, getters) => {
    if (
      gameView.game_players.length == 2 &&
      Object.entries(gameView.salvoes).length > 1
    ) {
      return Object.values(
        gameView.salvoes[
          gameView.game_players
            .flatMap(gp => gp.player_detail.id)
            .filter(id => id !== getters.currentUser.id)[0]
        ]
      ).flatMap(salvo => salvo);
    } else {
      return [];
    }
  },

  //Fleet Report
  fleetStatus: ({ gameView }) => gameView.fleet_status || {},

  //Server's game state
  gameState: ({ gameState }) => gameState
};

const mutations = {
  [SET_GAME_STATE_INFO]: (state, gameStateInfo) =>
    (state.gameState = {
      ...gameStateInfo
    }),

  [SET_GAME_VIEW_INFO]: (state, gameViewData) =>
    (state.gameView = {
      ...gameViewData
    }),

  [CLEAR_GAME_VIEW_INFO]: state => {
    state.gameView = {
      game_players: [],
      ships: [],
      salvoes: {}
    };
    state.gameState = {};
  }
};

const actions = {
  getGameViewInfo({ commit }, gpId) {
    fetch("/api/game_view/" + gpId)
      .then(response => response.json())
      .then(data => commit(SET_GAME_VIEW_INFO, data));
  },

  getGameState({ commit }, gpId) {
    fetch("/api/game_state/" + gpId)
      .then(response => response.json())
      .then(data => commit(SET_GAME_STATE_INFO, data));
  },

  clearGameViewInfo({ commit }) {
    commit(CLEAR_GAME_VIEW_INFO);
  }
};

export default {
  state,
  getters,
  mutations,
  actions
};
