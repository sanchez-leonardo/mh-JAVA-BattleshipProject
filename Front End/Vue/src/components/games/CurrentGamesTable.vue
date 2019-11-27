<template>
  <v-row justify="center" tag="article" no-gutters>
    <v-col cols="10">
      <h1 class="headline font-italic font-weight-medium text-left">Active Games</h1>
      <v-simple-table height="400px" fixed-header>
        <thead>
          <tr>
            <td></td>
          </tr>
          <th></th>
          <th class="title text-center">Player 1</th>
          <th class="title text-center">Player 2</th>
          <th class="title text-center">Created</th>
          <th></th>
        </thead>

        <tbody>
          <TableRow v-for="game in filteredGames" :game="game" :key="game.id" />
        </tbody>
      </v-simple-table>
      <v-row justify="center" v-if="filter">
        <v-col cols="auto">
          <v-checkbox
            v-model="filter"
            color="blue darken-3"
            value="blue darken-3"
            label="Hide Full and Finished Games"
            light
            v-if="currentUser"
          ></v-checkbox>
        </v-col>
      </v-row>
    </v-col>
  </v-row>
</template>

<script>
import { mapGetters } from "vuex";

import TableRow from "./CurrentGamesRow";

export default {
  name: "CurrentGames",
  components: {
    TableRow
  },

  data() {
    return {
      filter: false
    };
  },

  computed: {
    ...mapGetters(["currentUser", "currentGames"]),

    filteredGames() {
      if (this.filter) {
        return this.currentGames.filter(
          game =>
            (game.game_players.length == 1 || this.userInGame(game)) &&
            game.game_state != "over"
        );
      } else {
        return this.currentGames;
      }
    }
  },

  methods: {
    userInGame(game) {
      return game.game_players
        .map(gp => gp.player.id)
        .includes(this.currentUser.id);
    }
  }
};
</script>