<template>
  <v-app>
    <Header />
    <LogForms />
    <v-content tag="main">
      <router-view :key="$route.fullPath"></router-view>
    </v-content>
    <Footer />
  </v-app>
</template>

<script>
import Header from "./components/Header.vue";
import LogForms from "./components/LogForms";
import Footer from "./components/Footer.vue";

export default {
  name: "app",
  components: {
    Header,
    LogForms,
    Footer
  }
};
</script>
<style>
#app {
  background-image: url("./assets/backgrounds/warships-background.jpg");
  background-repeat: no-repeat;
  background-position: center;
  background-size: cover;
}
</style>
