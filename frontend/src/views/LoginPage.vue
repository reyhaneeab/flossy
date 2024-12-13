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
import {authService} from "@/authService";

export default {
  setup() {
    const email = ref('')
    const password = ref('')
    const error = ref('')
    const router = useRouter()

    const handleLogin = async () => {
      try {
        const response = authService.login(email, password);

        if (response) {
          console.log(1);
          const token = localStorage.getItem("token"); // Ensure token is set in storage
          if (token) {
            const decoded = jwtDecode(token); // Decode the token for debugging
            console.log("Decoded Token:", decoded);
            router.push("/home"); // Navigate to home page
          } else {
            error.value = "Token not found in local storage.";
          }
        }
      } catch (err) {
        console.error("Error during login:", err.message || "Unexpected error");
        error.value = err.response?.data?.message || "An unexpected error occurred.";
      }
    };




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