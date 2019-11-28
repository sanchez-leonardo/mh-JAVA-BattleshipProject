<template>
  <v-container>
    <v-form v-on:submit.prevent dense>
      <v-row justify="center" v-if="!currentUser">
        <v-col cols="3">
          <v-text-field
            id="userName"
            type="text"
            label="User Name"
            v-model="formData.userName"
            :rules="userNameRules"
          ></v-text-field>
        </v-col>

        <v-col cols="3">
          <v-text-field
            id="password"
            type="password"
            label="Password"
            v-model="formData.password"
            :rules="passwordRules"
          ></v-text-field>
        </v-col>

        <v-col cols="1">
          <v-btn block color="primary" id="log-in" @click.prevent="logIn">Log In!</v-btn>
        </v-col>

        <v-col cols="1">
          <v-btn block color="primary" id="sign-up" @click.prevent="signUp">Sign Up!</v-btn>
        </v-col>
      </v-row>

      <v-row justify="center" v-else>
        <v-col cols="4">
          <h2 class="text-center">Hello {{ currentUser.email }}</h2>
        </v-col>

        <v-col cols="2">
          <v-btn block color="primary" id="log-out" @click.prevent="logOut">Log Out!</v-btn>
        </v-col>
      </v-row>
    </v-form>

    <v-dialog v-model="dialog" max-width="500">
      <v-card>
        <v-card-title class="display-1 font-weight-medium">DENIED</v-card-title>
        <v-spacer></v-spacer>
        <v-card-text class="font-italic font-weight-medium">You tried to enter and failed miserably</v-card-text>
        <v-spacer></v-spacer>
        <v-card-text class="font-weight-medium">{{dialogMessage}}</v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="green darken-1" text @click="dialog = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script>
import { emailIsValid, customFetch } from "../scripts/utilities_script";
import { mapActions, mapGetters } from "vuex";

export default {
  name: "LogForms",

  data() {
    return {
      dialog: false,
      dialogMessage: "Check your email and password and try again",
      valid: false,
      formData: {
        userName: "",
        password: ""
      },
      userNameRules: [
        userName => emailIsValid(userName) || "Please, use a valid email"
      ],
      passwordRules: [
        password => password.length > 2 || "Password must be 6 characters long"
      ]
    };
  },

  computed: mapGetters(["currentUser"]),

  methods: {
    ...mapActions(["getGamesInfo"]),

    logIn() {
      let formFields = new URLSearchParams(this.formData);

      customFetch(
        "POST",
        "/api/login",
        [{ "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" }],
        formFields
      ).then(response => {
        if (response.ok) {
          this.getGamesInfo();
        } else {
          this.dialogMessage = "Incorrect password or username";
          this.dialog = !this.dialog;
        }
      });
    },
    signUp() {
      let formFields = new URLSearchParams(this.formData);
      customFetch(
        "POST",
        "/api/players",
        [{ "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" }],
        formFields
      ).then(response => {
        if (response.ok) {
          this.logIn();
        } else {
          this.dialogMessage = "Username already in use, most likely";
          this.dialog = !this.dialog;
        }
      });
    },
    logOut() {
      customFetch("POST", "/api/logout").then(response => {
        if (response.ok) {
          this.getGamesInfo();
          if (this.$route.path !== "/") {
            this.$router.push("/");
          }
        } else {
          this.dialogMessage =
            "I have no idea how you posible failed at logging out...";
          this.dialog = !this.dialog;
        }
      });
    }
  }
};
</script>