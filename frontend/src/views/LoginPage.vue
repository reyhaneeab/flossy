<template>
  <div class="login-container">
    <form @submit.prevent="handleLogin">
      <input
          type="email"
          v-model="email"
          placeholder="Email"
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
    <p v-if="error" class="error-message">{{ error }}</p>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const URL = `http://localhost:8090/api/users/login`

export default {
  setup() {
    const email = ref('')
    const password = ref('')
    const error = ref('')
    const router = useRouter()

    const handleLogin = async () => {
      try {
        const response = await fetch(URL, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            email: email.value,
            password: password.value,
          }),
          credentials: 'include',
        });

        if (response.ok) {
          const data = await response.json()
          localStorage.setItem('token', data.token)
          router.push('/home')
        } else {
          const errorData = await response.text();
          try {
            const parsedError = JSON.parse(errorData);
            error.value = parsedError.message || 'Login failed';
          } catch (e) {
            error.value = errorData || 'Login failed';
          }
        }
      } catch (err) {
        console.error('Login error:', err)
        error.value = 'An unexpected error occurred'
      }
    }

    return {
      email,
      password,
      error,
      handleLogin
    }
  }
}
</script>

<style scoped>
.error-message {
  color: red;
  margin-top: 10px;
}
</style>