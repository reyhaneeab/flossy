<template>
  <div id="app">
    <!-- Add a navigation bar or header if needed -->
    <header>
      <nav>
        <router-link to="/">Home</router-link>
        <router-link to="/about">About</router-link>
        <router-link to="/contact">Contact</router-link>
        <router-link to="/calendar">Calendar</router-link>
        <router-link to="/appointment">Appointment</router-link>
      </nav>
    </header>

    <!-- This is where the current view will be rendered -->
    <router-view />
  </div>
</template>

<script>
export default {
  name: "App",
};
</script>

<style>
/* Add global styles here */
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}

nav {
  display: flex;
  gap: 1rem;
  justify-content: center;
  background-color: #f8f9fa;
  padding: 1rem;
}

nav a {
  text-decoration: none;
  color: #2c3e50;
}

nav a.router-link-active {
  font-weight: bold;
  color: #42b983;
}
</style>