<template>
  <v-row class="grid-line flex-row" no-gutters>
    <grid-square
      v-for="(number, key) in 11"
      :key="key"
      :id="gridType + letter + (number - 1)"
      :class="'grid-square ' + mainClassManager(gridType, letter, number-1 ) "
      :gridType="gridType"
      :letter="letter"
      :number="number - 1"
    ></grid-square>
  </v-row>
</template>

<script>
import GridSquare from "./GridSquare";

export default {
  name: "GridLine",

  components: { "grid-square": GridSquare },

  props: {
    gridType: String,
    letter: String
  },

  methods: {
    mainClassManager(gridType, letter, number) {
      if (
        gridType + letter + number == "s00" ||
        gridType + letter + number == "p00"
      ) {
        return "blank";
      } else if (letter === "0") {
        return "column-name";
      } else if (number === 0) {
        return "column-num";
      } else if (gridType == "p" && number !== 0) {
        return "wah";
      } else if (gridType == "s" && number !== 0) {
        return "battle-square";
      }
    }
  }
};
</script>
