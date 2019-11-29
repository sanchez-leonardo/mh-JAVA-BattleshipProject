import Vue from "vue";
import Vuex from "vuex";

import gamesInfoModule from "./modules/gamesInfoModule";
import gameplayModule from "./modules/gameplayModule";

Vue.use(Vuex);

export default new Vuex.Store({
  modules: {
    gamesInfoModule,
    gameplayModule
  }
});
