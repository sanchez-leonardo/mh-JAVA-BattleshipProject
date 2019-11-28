<template>
  <v-row justify="center" tag="article">
    <v-col cols="8">
      <h1 class="headline font-italic font-weight-medium text-left">Leaderboard</h1>
      <v-simple-table>
        <thead>
          <th class="title text-center">Player</th>
          <th class="title text-center">Score</th>
          <th class="title text-center">Won</th>
          <th class="title text-center">Lost</th>
          <th class="title text-center">Tied</th>
        </thead>

        <tbody>
          <tr v-for="user in leaderboardInfo" :key="user.id">
            <td class="text-center">{{user.email}}</td>
            <td class="text-center">{{user.scores.total}}</td>
            <td class="text-center">{{user.scores.win}}</td>
            <td class="text-center">{{user.scores.loose}}</td>
            <td class="text-center">{{user.scores.tie}}</td>
          </tr>
        </tbody>
      </v-simple-table>
    </v-col>
  </v-row>
</template>

<script>
import { mapGetters } from "vuex";

export default {
  name: "Leaderboard",

  computed: mapGetters(["leaderboardInfo"])
};
</script>