<template>
  <div id="welcome-page">
    <h1>Welcome to the Dentist Office Management System</h1>
    <div>
      <button @click="showLogin">Login</button>
      <button @click="showSignup">Sign Up</button>
    </div>
    <Login v-if="showLoginForm" />
    <Signup v-if="showSignupForm" />
  </div>
</template>

<script>
import Login from '@/components/LogIn.vue';
import Signup from '@/components/SignUp.vue';

export default {
  name: 'WelcomePage',
  components: {
    Login,
    Signup
  },
  data() {
    return {
      showLoginForm: false,
      showSignupForm: false
    };
  },
  methods: {
    showLogin() {
      this.showLoginForm = true;
      this.showSignupForm = false;
    },
    showSignup() {
      this.showLoginForm = false;
      this.showSignupForm = true;
    }
  }
};
</script>

<style>
#welcome-page {
  text-align: center;
  padding: 20px;
}
button {
  margin: 10px;
  padding: 10px 20px;
  font-size: 16px;
  cursor: pointer;
}
</style>