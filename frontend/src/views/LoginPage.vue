<template>
  <div class="container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-6">
        <div class="card login-container">
          <div class="card-header">
            <h2 class="text-center">Login</h2>
          </div>
          <div class="card-body">
            <form @submit.prevent="handleLogin">
              <div class="mb-3">
                <label for="email" class="form-label">Email</label>
                <input
                  type="email"
                  class="form-control"
                  id="email"
                  v-model="email"
                  placeholder="Enter your email"
                  required
                />
              </div>
              <div class="mb-3">
                <label for="password" class="form-label">Password</label>
                <input
                  type="password"
                  class="form-control"
                  id="password"
                  v-model="password"
                  placeholder="Enter your password"
                  required
                />
              </div>
              <div class="d-grid">
                <button type="submit" class="btn btn-primary login-btn">Login</button>
              </div>
            </form>
            <p v-if="error" class="text-danger mt-3 error-message">{{ error }}</p>
          </div>
        </div>
      </div>
    </div>
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
.login-container {
  max-width: 400px;
  margin: 0 auto;
}

.login-btn {
  background-color: #007bff;
  border-color: #007bff;
}

.login-btn:hover {
  background-color: #0056b3;
  border-color: #0056b3;
}

.error-message {
  color: red;
  margin-top: 10px;
}
</style>