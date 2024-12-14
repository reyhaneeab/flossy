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
            <h1 class="title">Create Your Account!</h1>
            <p class="subtitle">Join our community today and start caring about your teeth!</p>
          </div>
          <!-- Card body for the form -->
          <div class="card-body">
            <!-- Form with prevent default on submit, calling handleSignup method -->
            <form @submit.prevent="handleSignup" class="signup-form">
              <!-- First Name input field -->
              <div class="mb-3">
                <label for="firstName" class="form-label">First Name</label>
                <input
                    id="firstName"
                  type="text"
                  class="form-control"
                  v-model="firstName"
                  placeholder="Enter your first name"
                  required
                />
              </div>
              <!-- Last Name input field -->
              <div class="mb-3">
                <label for="lastName" class="form-label">Last Name</label>
                <input
                    id="lastName"
                  type="text"
                  class="form-control"
                  v-model="lastName"
                  placeholder="Enter your last name"
                  required
                />
              </div>
              <!-- Email input field -->
              <div class="mb-3">
                <label for="email" class="form-label">Email</label>
                <input
                    id="email"
                  type="email"
                  class="form-control"
                  v-model="email"
                  placeholder="Enter your email"
                  required
                />
              </div>
              <!-- Phone input field -->
              <div class="mb-3">
                <label for="phone" class="form-label">Phone Number</label>
                <input
                    id="phone"
                  type="number"
                  class="form-control"
                  v-model="phone"
                  placeholder="Enter your phone number"
                  required
                />
              </div>
              <!-- Password input field -->
              <div class="mb-3">
                <label for="password" class="form-label">Password</label>
                <input
                    id="password"
                  type="password"
                  class="form-control"
                  v-model="password"
                  placeholder="Password"
                  required
                />
              </div>
              <!-- Confirm password input field -->
              <div class="mb-3">
                <label for="confirm-password" class="form-label">Confirm Password</label>
                <input
                    id="confirm-password"
                    v-model="confirmPassword"
                    type="password"
                    class="form-control"
                    placeholder="Confirm your password"
                    required
                />
              </div>

              <div v-if="error" class="error-message">{{error}}</div>

              <!-- Button container with full-width grid layout -->
              <div class="d-grid">
                <button type="submit" class="btn-primary">Sign Up</button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import router from "@/router";
import api from "@/api";

export default {
  name: 'SignUpPage',
  data(){
    return{
      firstName:"",
      lastName:"",
      email:"",
      phone:"",
      password:"",
      confirmPassword:"",
      error: null,
    };
  },
methods:{
  async handleSignup(){
    if(this.password !== this.confirmPassword){
      this.error = "Passwords do not match!";
      return;
    }
    try {
      const response = await api.post("/user/registration", {
        firstName: this.firstName,
        lastName: this.lastName,
        email: this.email,
        phone: this.phone,
        password: this.password
      })
      console.log('User created successfully', response.data)
      alert("User created successfully")
      router.push('/login')
    }
    catch (error){
      console.log(error)
      this.error=error
    }
  }

  }
}
</script>

<style scoped>

/* Card Styles */
.signup-container {
  background-color: #ffffff; /* Clean white card */
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); /* Subtle shadow for depth */
  padding: 2rem;
  width: 100%;
  height: 100%;
}

/* Title and Subtitle */
.title {
  font-size: 1.8rem;
  font-weight: bold;
  color: #343a40; /* Darker color for strong emphasis */
  margin-bottom: 0.5rem;
  text-align: center;
}

.subtitle {
  font-size: 1rem;
  color: #6c757d; /* Muted secondary text */
  text-align: center;
  margin-bottom: 1.5rem;
}

/* Form Styles */
.signup-form .form-label {
  font-weight: 500;
  color: #495057; /* Slightly darker for labels */
}

.signup-form .form-control {
  border: 1px solid #ced4da; /* Light border */
  border-radius: 6px;
  padding: 0.75rem;
  font-size: 1rem;
  transition: border-color 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}

.signup-form .form-control:focus {
  border-color: #00b8d9; /* Highlight with app's primary color */
  box-shadow: 0 0 4px rgba(0, 184, 217, 0.25);
  outline: none;
}

/* Error Message */
.error-message {
  color: #dc3545; /* Bootstrap danger color */
  font-size: 0.875rem;
  margin-top: -0.5rem;
  margin-bottom: 1rem;
}

/* Button Styles */
.btn-primary {
  background-color: #00b8d9; /* App's primary color */
  color: #fff;
  font-weight: 600;
  padding: 0.75rem;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  width: 100%;
  transition: background-color 0.2s ease-in-out, transform 0.1s ease-in-out;
}

.btn-primary:hover {
  background-color: #0094ad; /* Darker shade for hover effect */
  transform: translateY(-1px);
}

.btn-primary:active {
  background-color: #007a8d; /* Even darker for active */
  transform: translateY(0);
}

/* Responsive Design */
@media (max-width: 768px) {
  .signup-container {
    padding: 1.5rem;
  }

  .title {
    font-size: 1.6rem;
  }

  .subtitle {
    font-size: 0.9rem;
  }
}
</style>


