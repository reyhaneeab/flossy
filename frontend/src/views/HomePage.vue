<template>
  <div class="background">
    <div class="container mt-5">
      <div class="row justify-content-center">
        <!-- Column for content, centered and responsive -->
        <div class="col-md-8 text-center">
          <div class="content">
          <h1 class="display-4 mb-4">Welcome Home!</h1>
          <p class="lead">You are logged in.</p>
          <!-- Navigation buttons -->
          <div class="mt-5">
            <router-link to="/map" class="btn btn-primary btn-lg mr-3 home-btn">
              Interest List
            </router-link>
            <router-link to="/profile" class="btn btn-secondary btn-lg home-btn">
              My Profile
            </router-link>
          </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>

</script>

<style scoped>
/* Styles for the navigation buttons */
.home-btn {
  margin: 0 10px;
  transition: all 0.3s ease;
}
/* Hover effect for navigation buttons */
.home-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}
.background {
  background-image: url('../assets/background.jpg');
  background-size: cover;
  background-repeat: no-repeat;
  width: 100vw;
  height: 100vh;
  margin: 0;
  padding: 0;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.content {
  text-align: center; /* Centers text inside the content block */
}
h1 {
  color: #333;
  text-align: center;
}

p {
  text-align: center;
  color: #666;
}
/* Media query for responsive design on smaller screens */
@media (max-width: 768px) {
  .home-btn {
    display: block;
    width: 100%;
    margin: 10px 0;
  }
}
</style>