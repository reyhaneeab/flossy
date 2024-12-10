<template>
  <div class="container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-8 text-center">
        <h1 class="display-4 mb-4">Welcome Home!</h1>
        <p class="lead">You are logged in.</p>
        <div class="mt-5">
          <router-link to="/calendar" class="btn btn-primary btn-lg mr-3 home-btn">
            View Calendar
          </router-link>
          <router-link to="/profile" class="btn btn-secondary btn-lg home-btn">
            My Profile
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>

</script>

<style scoped>
.home-btn {
  margin: 0 10px;
  transition: all 0.3s ease;
}

.home-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

h1 {
  color: #333;
  text-align: center;
}

p {
  text-align: center;
  color: #666;
}

@media (max-width: 768px) {
  .home-btn {
    display: block;
    width: 100%;
    margin: 10px 0;
  }
}
</style>