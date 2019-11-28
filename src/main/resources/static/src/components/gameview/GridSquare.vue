<template>
  <v-col
    :class="['d-flex','justify-center','align-center',{piece: hasShip}, {hit: hasHit}, {salvo: hasSalvo}]"
  >
    <p class="ma-0" v-if="this.letter ==='0' || this.number === 0" v-text="contents()"></p>
    <DynamicImage
      :id="shipType"
      :class="['ship', 'image', {rotated: isRotated}]"
      :image="shipImagePath()"
      :alt="shipType"
      v-if="hasShipImage"
    />
    <DynamicImage
      :image="hitImagePath()"
      :alt="altHitOrSalvo"
      :class="[{salvo: hasSalvo},{hit: hasHit},'image']"
      v-if="hasHit || hasSalvo"
    />
  </v-col>
</template>

<script>
import { mapGetters } from "vuex";

import DynamicImage from "./DynamicImage";

export default {
  name: "GridSquare",

  components: {
    DynamicImage
  },

  props: { gridType: String, letter: String, number: Number },

  computed: {
    ...mapGetters([
      "playerShips",
      "playerSalvoesLocations",
      "opponentSalvoesLocations"
    ]),

    id() {
      return this.$attrs.id.slice(1);
    },

    playerShipsLocations() {
      return this.playerShips.flatMap(ship => ship.locations);
    },

    hasShip() {
      return (
        this.playerShipsLocations != null &&
        this.gridType === "p" &&
        this.playerShipsLocations.includes(this.id)
      );
    },
    hasHit() {
      return (
        this.opponentSalvoesLocations != null &&
        this.gridType === "p" &&
        this.opponentSalvoesLocations.includes(this.id) &&
        this.hasShip
      );
    },
    hasSalvo() {
      return (
        this.playerSalvoesLocations !== null &&
        this.gridType === "s" &&
        this.playerSalvoesLocations.includes(this.id)
      );
    },

    playerShipsStartingSquare() {
      return this.playerShips.map(ship => {
        return {
          type: ship.type,
          locations: ship.locations[0]
        };
      });
    },

    hasShipImage() {
      return (
        this.playerShipsStartingSquare
          .flatMap(locations => locations.locations)
          .includes(this.id) && this.gridType === "p"
      );
    },

    shipType() {
      if (this.hasShipImage) {
        return this.playerShipsStartingSquare.filter(ship =>
          ship.locations.includes(this.id)
        )[0].type;
      } else {
        return null;
      }
    },

    isRotated() {
      if (this.hasShipImage) {
        let theShip = this.playerShips.filter(
          ship => ship.type === this.shipType
        )[0];

        return theShip.locations
          .map(loc => loc.slice(1))
          .every((val, i, arr) => val === arr[0]);
      } else {
        return null;
      }
    },

    altHitOrSalvo() {
      if (this.hasHit) {
        return "hit";
      } else if (this.hasSalvo) {
        return "salvo";
      } else {
        return "";
      }
    }
  },

  methods: {
    contents() {
      if (this.letter === "0" && this.number === 0) {
        return "";
      }
      if (this.letter === "0") {
        return this.number;
      }
      if (this.number === 0) {
        return this.letter.toUpperCase();
      }
    },

    shipImagePath() {
      if (this.hasShipImage) {
        return "ships/" + this.shipType + "hor.png";
      }
    },

    hitImagePath() {
      if (this.hasHit) {
        return "hit3.gif";
      }

      if (this.hasSalvo) {
        return "target.png";
      }
    }
  }
};
</script>
