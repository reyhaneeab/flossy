<template>
  <!-- Container with top margin for centering and spacing -->
  <div class="container mt-5">
    <!-- Row for horizontal centering -->
    <div class="row justify-content-center">
      <!-- Column for responsive width control
           On medium screens and larger, it takes up half the container width -->
      <div class="col-md-6">
        <!-- Card component for visual grouping -->
        <div class="card login-container">
          <!-- Card header for the title -->
          <div class="card-header">
            <h2 class="text-center">Login</h2>
          </div>
          <!-- Card body for the form -->
          <div class="card-body">
            <!-- Form with prevent default on submit -->
            <form @submit.prevent="handleLogin">
              <!-- Email input group -->
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
              <!-- Password input group -->
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
              <!-- Button container with full-width grid layout -->
              <div class="d-grid">
                <button type="submit" class="btn btn-primary login-btn">Login</button>
              </div>
            </form>
            <!-- Error message, displayed conditionally -->
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
import {jwtDecode} from "jwt-decode";

const URL = `http://localhost:8091/api/user/login`

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
          const decoded = jwtDecode(data.token);
          console.log(decoded);
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