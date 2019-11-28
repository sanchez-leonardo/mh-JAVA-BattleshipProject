<template>
  <v-simple-table dark>
    <thead>
      <th class="title text-center">Ship</th>
      <th class="title text-center">Damage</th>
      <th class="title text-center">Locations</th>
    </thead>
    <tbody>
      <tr v-for="(ship, key) in fleet" :key="key">
        <td class="text-left">{{ship | shipName}}</td>
        <DamageCell :shipType="ship" :damage="shipDamage(ship).length"></DamageCell>
        <td class="text-left">{{shipDamage(ship) | concatLocations}}</td>
      </tr>
    </tbody>
  </v-simple-table>
</template>

<script>
import { mapGetters } from "vuex";

import DamageCell from "./FleetStatusDamageCell";

export default {
  name: "FleetStatusTable",

  components: { DamageCell },

  props: ["playerId"],

  data() {
    return {
      fleet: ["carrier", "battleship", "destroyer", "submarine", "patrol_boat"]
    };
  },

  computed: {
    ...mapGetters(["fleetStatus"]),

    fleetByTurn() {
      return this.fleetStatus[this.playerId] || [];
    }
  },

  methods: {
    shipDamage(type) {
      return this.fleetByTurn
        .flatMap(turn => turn.hits)
        .filter(obj => obj.ship === type)
        .flatMap(sub => sub.dmg);
    }
  },

  filters: {
    shipName(string) {
      return (string.charAt(0).toUpperCase() + string.slice(1)).replace(
        "_",
        " "
      );
    },

    concatLocations(string) {
      return string.join(", ").toUpperCase();
    }
  }
};
</script>