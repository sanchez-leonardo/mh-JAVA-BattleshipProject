<template>
  <v-container tag="section" fluid>
    <v-divider></v-divider>
    <Leaderboard />
    <GamesTable />
    <v-row justify="space-around" v-if="currentUser">
      <v-col cols="3">
        <v-btn block color="primary" id="refresh-games" @click="updateGamesList">Refresh</v-btn>
      </v-col>
      <v-col cols="3">
        <v-btn block color="primary" id="create-game" @click="createGame">Create Game</v-btn>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { customFetch } from "../../scripts/utilities_script";

import Leaderboard from "./LearderboardTable";
import GamesTable from "./CurrentGamesTable";
import { mapGetters, mapActions } from "vuex";

export default {
  name: "Games",

  components: {
    Leaderboard,
    GamesTable
  },

  computed: mapGetters(["currentUser"]),

  methods: {
    ...mapActions(["getLeaderboardInfo", "getGamesInfo"]),

    updateGamesList() {
      this.getGamesInfo();
    },

    createGame() {
      customFetch("POST", "/api/games")
        .then(response => {
          if (response.ok) {
            response.json().then(data =>
              this.$router.push({
                name: "GameView",
                params: { id: data.gpId.toString() }
              })
            );
          } else {
            alert("Something went south, try again later");
          }
        })
        // eslint-disable-next-line no-console
        .catch(error => console.log(error));
    }
  },

  created() {
    this.getLeaderboardInfo();
    this.getGamesInfo();
  }
};
</script>