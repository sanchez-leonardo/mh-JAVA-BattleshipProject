<template>
  <v-container tag="section" fluid>
    <v-row justify="center" class="mb-4 mt-2" no-gutters>
      <v-col cols="3">
        <v-btn block color="primary" id="back-to-games" @click="goToGames">Go Back</v-btn>
      </v-col>
    </v-row>

    <v-row justify="space-around">
      <Grid :gridName="playerGrid" :shipsOrSalvoes="shipsOrSalvoes" />
      <v-divider vertical v-if="shipsOrSalvoes === 'salvoes'"></v-divider>
      <Grid
        :gridName="salvoGrid"
        :shipsOrSalvoes="shipsOrSalvoes"
        v-if="shipsOrSalvoes === 'salvoes'"
      />
    </v-row>

    <v-dialog v-model="dialogPopUp" max-width="500">
      <v-card>
        <v-card-title class="text-center display-1 font-weight-medium">{{dialogTitle}}</v-card-title>
        <v-spacer></v-spacer>
        <v-card-text class="font-weight-medium">{{dialogMessage}}</v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="green darken-1" text @click="goToGames">To Games Screen</v-btn>
          <v-btn color="red darken-1" text @click="dialog = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script>
import { mapGetters, mapActions } from "vuex";

import Grid from "./Grid";

export default {
  name: "GameView",

  components: {
    Grid
  },

  data() {
    return {
      playerGrid: "player",
      salvoGrid: "salvo",
      dialog: true,
      intervalId: ""
    };
  },

  computed: {
    ...mapGetters(["currentUser", "gameViewState", "gameState"]),

    shipsOrSalvoes({ gameViewState }) {
      if (
        (gameViewState.game_state === "ship" ||
          gameViewState.game_state === "waiting_p2") &&
        gameViewState.game_player_state === "placing"
      ) {
        return "ships";
      } else {
        return "salvoes";
      }
    },

    dialogPopUp({ gameViewState }) {
      return this.dialog && gameViewState.game_state === "over";
    },

    dialogTitle({ gameViewState }) {
      if (gameViewState.game_player_state === "win") {
        return "WINNER";
      } else if (gameViewState.game_player_state === "loss") {
        return "LOOSER";
      } else if (gameViewState.game_player_state === "tie") {
        return "TIED";
      } else {
        return "ERROR!";
      }
    },
    dialogMessage({ gameViewState }) {
      if (gameViewState.game_player_state === "win") {
        return "Congratulations! You've killed an approximate of 10000 men and women";
      } else if (gameViewState.game_player_state === "loss") {
        return "The red tainted seas are a testament of your failure";
      } else if (gameViewState.game_player_state === "tie") {
        return "It's hard to understand if you are both formidable strategists or an equal dissapointment";
      } else {
        return "If you are reading this, contact the devs!";
      }
    }
  },

  methods: {
    ...mapActions([
      "getGamesInfo",
      "getGameViewInfo",
      "clearGameViewInfo",
      "getGameState"
    ]),

    goToGames() {
      this.$router.push("/");
    },

    goToGamesFromModal() {
      this.dialog = false;
      this.$router.push("/");
    },

    checkForChanges() {
      let id = setInterval(this.changesChange, 10000);

      this.intervalId = id;
    },

    async changesChange() {
      await this.getGameState(this.$route.params.id.toString());

      if (
        this.gameViewState.game_state != this.gameState.game_state ||
        this.gameViewState.game_turn == this.gameState.game_turn + 1 ||
        this.gameViewState.game_player_state != this.gameState.game_player_state
      ) {
        this.getGameViewInfo(this.$route.params.id.toString());
      } else if (this.gameViewState.game_state == "over") {
        window.clearInterval(this.intervalId);
        this.intervalId = "";
      }
    }
  },

  created() {
    if (!this.currentUser) {
      this.getGamesInfo();
    }

    this.getGameViewInfo(this.$route.params.id.toString()).then(() =>
      this.checkForChanges()
    );
  },

  beforeDestroy() {
    this.clearGameViewInfo();

    if (this.gameState.game_state != "over") {
      window.clearInterval(this.intervalId);
    }

    window.salvoesForPost.length = 0;
  }
};
</script>
