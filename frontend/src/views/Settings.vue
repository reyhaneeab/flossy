<template>
   <div class="container mt-5">
     <div class="row justify-content-center">
      <!-- Column for responsive width control
           On medium screens and larger, it takes up half the container width -->
      <div class="col-md-6">
        <!-- Card component for visual grouping -->
        <div class="card success-container">
          <!-- Card body for content -->
          <div class="card-body">
            <!-- Success message -->
            <h1 class="card-title">Registration Successful!</h1>
            <!-- Router link to the login page, styled as a button -->
            <router-link to="/login" class="btn btn-primary login-link">Go to Login</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>

export default {

  name: 'Success'
}
</script>

<style scoped>
/* Styles for the success container */
.success-container {
  text-align: center;
  padding: 20px;
}

/* Styles for the heading */
h1 {
  color: #28a745; /* Green color for success */
  margin-bottom: 20px;
}

/* Styles for the login link */
.login-link {
  display: inline-block;
  padding: 10px 20px;
  background-color: #007bff; /* Bootstrap primary blue */
  color: white;
  text-decoration: none;
  border-radius: 4px;
}

/* Hover effect for the login link */
.login-link:hover {
  background-color: #0056b3; /* Darker blue on hover */
}
</style>