<template>
  <tr>
    <td class="text-center">{{game.id}}</td>
    <td class="text-center">{{player}}</td>
    <td class="text-center">{{oponent}}</td>
    <td class="text-center">{{game.created}}</td>
    <StatusCell v-if="currentUser" :content="statusCell" />
  </tr>
</template>

<script>
import { mapGetters } from "vuex";

import StatusCell from "./GameStatusCell";

export default {
  name: "CurrentGamesRow",

  components: {
    StatusCell
  },

  props: {
    game: Object
  },

  computed: {
    ...mapGetters(["currentUser"]),

    player() {
      return this.game.game_players[0].player.email;
    },

    oponent() {
      if (this.game.game_players.length == 2) {
        return this.game.game_players[1].player.email;
      } else {
        return "Waiting Player";
      }
    },

    userInGame() {
      return this.game.game_players
        .map(gp => gp.player.id)
        .includes(this.currentUser.id);
    },

    userGpId() {
      if (this.userInGame) {
        return this.game.game_players.find(
          gp => gp.player.id === this.currentUser.id
        ).id;
      } else {
        return null;
      }
    },

    statusCell() {
      if (this.game.game_state == "over") {
        return {
          tag: "cell",
          type: "text",
          content: "Game Over",
          id: null
        };
      } else {
        if (this.userInGame) {
          return {
            tag: "btn",
            type: "rejoin",
            content: "Re-Join",
            id: this.userGpId.toString()
          };
        } else if (!this.userInGame && this.game.game_players.length === 1) {
          return {
            tag: "btn",
            type: "join",
            content: "Join",
            id: this.game.id.toString()
          };
        } else {
          return {
            tag: "cell",
            type: "text",
            content: "Game Full",
            id: null
          };
        }
      }
    }
  }
};
</script>