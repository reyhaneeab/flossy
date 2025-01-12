<template>
  <div>
    <h2>Sign Up</h2>
    <form @submit.prevent="signup">
      <div>
        <label for="name">Your Office Name:</label>
        <input type="text" id="name" v-model="name" required />
      </div>
      <div>
        <label for="email">Email:</label>
        <input type="email" id="email" v-model="email" required />
      </div>
      <div>
        <label for="password">Password:</label>
        <input type="password" id="password" v-model="password" required />
      </div>
      <div>
        <label for="phone">Emergency Phone Number:</label>
        <input type="tel" id="phone" v-model="phone" required />
      </div>
      <div>
        <label for="address">Address:</label>
        <input type="text" id="address" v-model="address" required />
      </div>
      <button type="submit">Create Your Business</button>
    </form>
  </div>
</template>

<script>
import router from "@/router";
import apiClient from "@/api";

export default {
  data() {
    return {
      name: '',
      email: '',
      password: '',
      address: '',
      phone: '',
    };
  },
  methods: {
    async signup() {
      await apiClient.post("/dentist/registration",
          {officeName: this.name, email: this.email, password: this.password, address: this.address, phone: this.phone}).then((response) => {
        console.log(response);
      })
      alert('Dentist Office registered successfully!');
      await router.push('/schedule');
    }
  }
};
</script>