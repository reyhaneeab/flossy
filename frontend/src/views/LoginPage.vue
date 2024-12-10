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
import { useRouter } from 'vue-router'

    const URL = `http://localhost:8091/api/user/registration`
    const router = useRouter()



    const lastName = ref('')
    const firstName = ref('')
    const email = ref('')
    const phone = ref('')
    const password = ref('')

    async function handleSignup() {
      try {
        const response = await fetch(URL, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            email: email.value,
            firstName: firstName.value,
            lastName: lastName.value,
            phone: phone.value,
            password: password.value
          })
        })

        if (response.ok) {
          // Redirect to success page or login
          await router.push('/')
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