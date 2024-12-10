<template>
  <div class="container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-6">
        <div class="card success-container">
          <div class="card-body">
            <h1 class="card-title">Registration Successful!</h1>
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
.success-container {
  text-align: center;
  padding: 20px;
}

h1 {
  color: #28a745;
  margin-bottom: 20px;
}

.login-link {
  display: inline-block;
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  text-decoration: none;
  border-radius: 4px;
}

.login-link:hover {
  background-color: #0056b3;
}
</style>