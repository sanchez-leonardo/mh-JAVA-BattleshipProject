<template>
  <v-col cols="auto" tag="article">
    <v-row>
      <h1 class="headline font-italic font-weight-medium text-left ma-2">{{ gridTitle }}</h1>
    </v-row>

    <ShipsContainer v-if="shipsOrSalvoes === 'ships'" class="ma-2 mx-auto" />
    <div :id="gridName + '-grid'" class="d-flex flex-column ma-2 mx-auto">
      <GridLine
        v-for="(gridLetter, key) in gridLetters"
        :key="key"
        :letter="gridLetter"
        :gridType="gridType"
      />
    </div>

    <v-row justify="space-around" class="ma-2" no-gutters>
      <v-col cols="auto" v-if="shipsOrSalvoes === 'ships' && gridType === 'p'">
        <v-btn medium color="primary" id="post-ships" @click.prevent="postShipList">Place Ships!</v-btn>
      </v-col>

      <v-col
        cols="auto"
        v-if="shipsOrSalvoes === 'salvoes' && gridType === 'p'"
        justify-self="start"
      >
        <h2>In case you cannot wait 10 secs...</h2>
      </v-col>

      <v-col cols="auto" v-if="shipsOrSalvoes === 'salvoes' && gridType === 'p'">
        <v-btn medium color="primary" id="post-salvo" @click.prevent="getGameViewData">Refresh!</v-btn>
      </v-col>

      <v-col
        cols="auto"
        v-if="shipsOrSalvoes === 'salvoes' && gridType === 's'"
        justify-self="start"
      >
        <h2>Salvoes left: {{ salvoesLeft() }}</h2>
      </v-col>

      <v-col cols="auto" v-if="shipsOrSalvoes === 'salvoes' && gridType === 's'">
        <v-btn medium color="primary" id="post-salvo" @click.prevent="postSalvoesList">Fire!</v-btn>
      </v-col>
    </v-row>

    <v-row v-if="shipsOrSalvoes === 'salvoes'" justify="center">
      <v-col cols="auto">
        <FleetStatus :playerId="playerId()" />
      </v-col>
    </v-row>

    <v-dialog v-model="dialog" max-width="500">
      <v-card>
        <v-card-title class="text-center display-1 font-weight-medium">{{dialogTitle}}</v-card-title>
        <v-spacer></v-spacer>
        <v-card-text class="font-weight-medium">{{dialogMessage}}</v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="green darken-1" text @click="dialog = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-col>
</template>

<script>
/* eslint-disable no-console */
import { mapActions, mapGetters } from "vuex";
import { customFetch } from "../../scripts/utilities_script";

import ShipsContainer from "./ShipsContainer";
import GridLine from "./GridLine";
import FleetStatus from "./FleetStatusTable";

export default {
  name: "Grid",

  components: {
    GridLine,
    ShipsContainer,
    FleetStatus
  },

  props: { gridName: String, shipsOrSalvoes: String },

  data() {
    return {
      gridLetters: ["0", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j"],
      salvoes: window.salvoesForPost,
      dialog: false,
      dialogTitle: "Something went catastrophic",
      dialogMessage: "There was a serious issue somewhere"
    };
  },

  computed: {
    ...mapGetters(["currentUser", "gamePlayers", "opponentId"]),

    gridType() {
      if (this.gridName === "player") {
        return "p";
      } else {
        return "s";
      }
    },

    gpId() {
      return this.$route.params.id.toString();
    },

    gridTitle() {
      if (this.gridName === "player") {
        return "Your Grid";
      } else {
        if (this.gamePlayers.length === 2) {
          return (
            this.gamePlayers.filter(
              gp => gp.player_detail.id === this.opponentId
            )[0].player_detail.email + "'s Grid"
          );
        } else {
          return "Waiting for a worthy opponent";
        }
      }
    }
  },

  methods: {
    ...mapActions(["getGameViewInfo"]),

    playerId() {
      if (this.gridName === "player") {
        return this.currentUser.id;
      } else {
        return this.opponentId;
      }
    },

    getGameViewData() {
      this.getGameViewInfo(this.gpId);
    },

    postShipList() {
      if (window.shipsForPost.length === 5) {
        customFetch(
          "POST",
          "/api/games/players/" + this.gpId + "/ships",
          [
            {
              "Content-Type": "application/json;charset=UTF-8"
            }
          ],
          JSON.stringify(window.shipsForPost)
        )
          .then(response => {
            if (response.ok) {
              this.getGameViewData();
            } else {
              this.dialogTitle = "Shipping problems";
              this.dialogMessage = "Your order was not received";
              this.dialog = !this.dialog;
            }
          })
          .catch(error => console.log(error));
      } else {
        this.dialogTitle = "Don't be a hippie";
        this.dialogMessage = "Place all your ships to continue";
        this.dialog = !this.dialog;
      }
    },

    postSalvoesList() {
      if (
        window.salvoesForPost.length != 0 &&
        window.salvoesForPost.length <= 5
      ) {
        customFetch(
          "POST",
          "/api/games/players/" + this.gpId + "/salvoes",
          [
            {
              "Content-Type": "application/json;charset=UTF-8"
            }
          ],
          JSON.stringify(window.salvoesForPost)
        )
          .then(response => {
            if (response.ok) {
              this.getGameViewData();
              window.salvoesForPost.length = 0;
            } else {
              this.dialogTitle = "Hakuna your tatas";
              this.dialogMessage = "Wait for your opponent's move";
              this.dialog = !this.dialog;
            }
          })
          .catch(error => console.log(error));
      } else {
        this.dialogTitle = "Harsh words won't sink ships";
        this.dialogMessage = "Place your shots";
        this.dialog = !this.dialog;
      }
    },

    salvoesLeft() {
      return 5 - this.salvoes.length;
    }
  },

  mounted() {
    if (this.gridType === "s") {
      window.salvoesListeners();
    }
  }
};
</script>

<style>
/* Grid */
#player-grid,
#salvo-grid {
  width: 500px;
  height: 500px;
}
.grid-line {
  max-height: auto;
}
.grid-square {
  position: relative;
  border: solid 1px black;
}
.column-name,
.column-num {
  background-color: #455a64;
}
.column-name > p,
.column-num > p {
  color: white;
}
.blank {
  background-color: #263238;
}
.wah,
.battle-square {
  background-color: #1565c0;
}
.piece {
  background-color: #80cbc4;
}
.piece.hit {
  background-color: #ef5350;
}
.salvo {
  background-color: #78909c;
}
.salvo.impact {
  background-color: #ef5350;
}
.salvo.image {
  height: 100%;
  width: 100%;
}
.shot {
  background-color: #ff8f00;
}
/* Ship management */
#player {
  display: flex;
  flex-direction: column;
}
.available-ships {
  width: 500px;
  display: flex;
  height: 30px;
}
.holder {
  position: relative;
}
.image {
  position: absolute;
  z-index: 2;
  left: 0;
}
.shadow {
  opacity: 0.5;
  position: relative;
  z-index: 0;
}
.holder .image {
  width: 100%;
}
.wah .image {
  height: 100%;
}
.space {
  background-color: #4caf50;
}
.noSpace {
  background-color: #f44336;
}
.rotate-btn {
  position: absolute;
  z-index: 5;
  top: 25%;
  left: 25%;
}
.btn-img {
  color: white;
}
.rotated {
  transform: rotate(90deg);
}
#carrier.rotated {
  transform-origin: 10% 60%;
}
#battleship.rotated {
  transform-origin: 13% 60%;
}
#destroyer.rotated {
  transform-origin: 17% 55%;
}
#submarine.rotated {
  transform-origin: 17% 60%;
}
#patrol_boat.rotated {
  transform-origin: 25% 55%;
}
</style>
