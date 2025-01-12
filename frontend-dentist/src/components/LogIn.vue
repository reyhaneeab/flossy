<template>
  <div>
    <h2>Login</h2>
    <form @submit.prevent="login">
      <div>
        <label for="email">Email:</label>
        <input type="email" id="email" v-model="email" required />
      </div>
      <div>
        <label for="password">Password:</label>
        <input type="password" id="password" v-model="password" required />
      </div>
      <button type="submit">Login</button>
    </form>
  </div>
</template>

<script>
import router from "@/router";
import apiClient from "@/api";

export default {
  data() {
    return {
      email: '',
      password: ''
    };
  },
  methods: {
    async login() {
      await apiClient.get(`dentist/${this.email}`).then(response => {
        if(response.data.password !== this.password) {
          alert('Login failed. Please try again');
        }
        else{
          alert('Login successful!');
          localStorage.setItem('id', response.data.id);
          router.push('/schedule');
        }
      })
    }
  }
};
</script>