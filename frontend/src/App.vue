<template>
  <div id="app">
    <header>
      <nav class="navbar navbar-expand-lg navbar-dark">
        <div class="container">
          <a class="navbar-brand fw-bold" href="#">Flossy</a>
          <button
              class="navbar-toggler"
              type="button"
              data-bs-toggle="collapse"
              data-bs-target="#navbarNav"
              aria-controls="navbarNav"
              aria-expanded="false"
              aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
          </button>
          <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav me-auto">
              <li class="nav-item">
                <router-link class="nav-link" to="/home">Home</router-link>
              </li>
              <li class="nav-item">
                <router-link class="nav-link" to="/about">About</router-link>
              </li>
              <li class="nav-item">
                <router-link class="nav-link" to="/contact">Contact</router-link>
              </li>
              <li class="nav-item">
                <router-link class="nav-link" to="/calendar">Calendar</router-link>
              </li>
              <li class="nav-item">
                <router-link class="nav-link" to="/map">Map</router-link>
              </li>
              <li class="nav-item">
                <router-link class="nav-link" to="/appointments">My Appointments</router-link>
              </li>
            </ul>
            <ul class="navbar-nav" v-if="!loggedIn">
              <li class="nav-item dropdown" v-if="authView === 'Login'">
                <router-link class="nav-link auth-button" to="/signup" @click="toggleAuthView">
                  Sign Up
                </router-link>
              </li>
              <li class="nav-item dropdown" v-else-if="authView === 'Sign Up'">
                <router-link class="nav-link auth-button" to="/login" @click="toggleAuthView">
                  Log In
                </router-link>
              </li>
            </ul>
            <div class="navbar-nav" v-else>
              <button class="nav-item dropdown" @click="logout">
                Log Out
              </button>
            </div>
          </div>
        </div>
      </nav>
    </header>

    <main class="container mt-4">
      <router-view/>
    </main>
  </div>
</template>

<script>
import {authService} from "@/authService";
import router from "@/router";

export default {
  name: "App",
  data() {
    return {
      loggedIn: authService.isLoggedIn(),
      authView: "Login",
    };
  },
  methods: {
    toggleAuthView() {
      this.authView = this.authView === "Login" ? "Sign Up" : "Login";
    },
    async logout(){
      authService.logout();
      this.loggedIn = authService.isLoggedIn()
      await router.push('/login')
    }
  },
  mounted() {
    this.loggedIn = authService.isLoggedIn()
  },
};
</script>

<style>
  /* General Layout */
#app {
  font-family: "Inter", Arial, sans-serif;
  background-color: #0d1b2a;
  color: #e0e1dd;
  min-height: 100vh;
  width: 100%;
  height: 100%;
  display: grid;
}

/* Navbar Styling */
.navbar {
  background-color: #1b263b;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
  padding: 1rem;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.navbar-brand {
  font-size: 1.8rem;
  font-weight: bold;
  color: #e0e1dd;
  transition: color 0.3s;
}

.navbar-brand:hover {
  color: #f4d35e;
}

.nav-link {
  color: #e0e1dd;
  margin-right: 1rem;
  font-size: 1rem;
  transition: color 0.3s, transform 0.2s;
}

.nav-link:hover {
  color: #f4a261;
  transform: scale(1.1);
}

.auth-button {
  background-color: #f4a261;
  color: #ffffff;
  border-radius: 20px;
  padding: 0.5rem 1rem;
  text-align: center;
  transition: background-color 0.3s;
}

.auth-button:hover {
  background-color: #1b263b;
}

.navbar-toggler {
  border: none;
}

.navbar-toggler-icon {
  background-image: url("data:image/svg+xml;charset=utf8,%3Csvg viewBox='0 0 30 30' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath stroke='rgba%28224, 224, 224, 1%29' stroke-width='2' stroke-linecap='round' stroke-miterlimit='10' d='M4 7h22M4 15h22M4 23h22'/%3E%3C/svg%3E");
}

/* Main Content */
.container {
  flex: 1;
  padding: 2rem;
}

/* Mobile Responsiveness */
@media (max-width: 768px) {
  .navbar-nav {
    text-align: center;
  }
  .auth-button {
    margin-top: 1rem;
  }
}
</style>
