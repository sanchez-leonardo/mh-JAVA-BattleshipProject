<template>
  <td class="text-center">
    <v-btn block color="error" v-if="isButton" v-on:click="btnMethod">{{content.content}}</v-btn>
    <p v-if="!isButton">{{content.content}}</p>
  </td>
</template>


<script>
import { customFetch } from "../../scripts/utilities_script";

export default {
  props: { content: Object },

  computed: {
    isButton() {
      return this.content.tag === "btn";
    }
  },

  methods: {
    joinGame(gameId) {
      return customFetch("POST", "/api/game/" + gameId + "/players")
        .then(response => response.json())
        .then(data => data.gpId.toString());
    },

    goToGameView(id) {
      this.$router.push({ name: "GameView", params: { id } });
    },

    btnMethod() {
      if (this.content.type === "join") {
        this.joinGame(this.content.id).then(gpId => this.goToGameView(gpId));
      } else if (this.content.type === "rejoin") {
        this.goToGameView(this.content.id);
      }
    }
  }
};
</script>