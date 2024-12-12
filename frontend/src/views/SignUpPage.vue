<template>
   <div class="container mt-5">
    <!-- Row for horizontal centering -->
    <div class="row justify-content-center">
      <!-- Column for responsive width control
           On medium screens and larger, it takes up half the container width -->
      <div class="col-md-6">
        <!-- Card component for visual grouping -->
        <div class="card signup-container">
          <!-- Card header for the title -->
          <div class="card-header">
            <h1 class="text-center">Sign Up</h1>
          </div>
          <!-- Card body for the form -->
          <div class="card-body">
            <!-- Form with prevent default on submit, calling handleSignup method -->
            <form @submit.prevent="handleSignup">
              <!-- First Name input field -->
              <div class="mb-3">
                <input
                  type="text"
                  class="form-control"
                  v-model="firstName"
                  placeholder="First Name"
                  required
                />
              </div>
              <!-- Last Name input field -->
              <div class="mb-3">
                <input
                  type="text"
                  class="form-control"
                  v-model="lastName"
                  placeholder="Last Name"
                  required
                />
              </div>
              <!-- Email input field -->
              <div class="mb-3">
                <input
                  type="email"
                  class="form-control"
                  v-model="email"
                  placeholder="Email"
                  required
                />
              </div>
              <!-- Phone input field -->
              <div class="mb-3">
                <input
                  type="tel"
                  class="form-control"
                  v-model="phone"
                  placeholder="Phone"
                  required
                />
              </div>
              <!-- Password input field -->
              <div class="mb-3">
                <input
                  type="password"
                  class="form-control"
                  v-model="password"
                  placeholder="Password"
                  required
                />
              </div>
              <!-- Button container with full-width grid layout -->
              <div class="d-grid">
                <button type="submit" class="btn btn-success">Sign Up</button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
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
      // Redirect to home page on successful signup
      await router.push('/login')
      console.log('Signup successful')
    } else {
      console.error('Signup failed')
    }
  } catch (error) {
    console.error('Signup error:', error)
  }
}
</script>

<style scoped>
/* Styles for the signup container */
.signup-container {
  max-width: 400px;
  margin: 0 auto;
  padding: 20px;
}

/* Styles for the heading */
h1 {
  text-align: center;
  color: #333;
}

/* Styles for the form layout */
form {
  display: flex;
  flex-direction: column;
}

/* Styles for input fields */
input {
  margin-bottom: 15px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

/* Styles for the submit button */
button {
  padding: 10px;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

/* Hover effect for the submit button */
button:hover {
  background-color: #218838;
}
</style>