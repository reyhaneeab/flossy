<template>
  <div class="login-container">
    <form @submit.prevent="handleLogin">
      <input
          type="text"
          v-model="username"
          placeholder="Username"
          required
      />
      <input
          type="password"
          v-model="password"
          placeholder="Password"
          required
      />
      <button type="submit">Login</button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const username = ref('')
const password = ref('')

const handleLogin = async () => {
  try {
    // Add login logic here
    // Typically this would involve an API call to validate credentials
    const response = await fetch('/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: username.value,
        password: password.value
      })
    })

    if (response.ok) {
      // Redirect to home page or dashboard

      this.$router.push('/home')
      console.log('Login successful')
    } else {
      // Handle login failure
      console.error('Login failed')
    }
  } catch (error) {
    console.error('Login error:', error)
  }
}
</script>

<style scoped>
.login-container {
  max-width: 300px;
  margin: 0 auto;
  padding: 20px;
}

form {
  display: flex;
  flex-direction: column;
}

input {
  margin-bottom: 10px;
  padding: 10px;
}

button {
  padding: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  cursor: pointer;
}

button:hover {
  background-color: #0056b3;
}
</style>