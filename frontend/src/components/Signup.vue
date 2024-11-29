<template>
  <div class="signup-container">
    <h1>Signup Page</h1>
    <form @submit.prevent="handleSignup">
      <input
          type="text"
          v-model="firstName"
          placeholder="First Name"
          required
      />
      <input
          type="text"
          v-model="lastName"
          placeholder="Last Name"
          required
      />
      <input
          type="email"
          v-model="email"
          placeholder="Email"
          required
      />
      <input
          type="tel"
          v-model="phone"
          placeholder="Phone"
          required
      />
      <input
          type="password"
          v-model="password"
          placeholder="Password"
          required
      />
      <button type="submit">Sign Up</button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const firstName = ref('')
const lastName = ref('')
const email = ref('')
const phone = ref('')
const password = ref('')

const handleSignup = async () => {
  try {
    const response = await fetch('/api/users/signup/form', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        firstName: firstName.value,
        lastName: lastName.value,
        email: email.value,
        phone: phone.value,
        password: password.value
      })
    })

    if (response.ok) {
      // Redirect to success page or login
      this.$router.push('/signup/success')
      console.log('Signup successful')
    } else {
      // Handle signup failure
      console.error('Signup failed')
    }
  } catch (error) {
    console.error('Signup error:', error)
  }
}
</script>

<style scoped>
.signup-container {
  max-width: 400px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  text-align: center;
  color: #333;
}

form {
  display: flex;
  flex-direction: column;
}

input {
  margin-bottom: 15px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

button {
  padding: 10px;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #218838;
}
</style>