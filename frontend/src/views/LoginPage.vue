<template>
  <div class="container">
    <div class="auth-container" :class="{ 'signup-mode': isSignUp }">
      <!-- Left Panel -->
      <div class="auth-panel left-panel">
        <div class="content">
          <h2>Welcome Back!</h2>
          <p>To keep connected with us, please login with your personal info.</p>
          <button @click="setSignUp(false)" class="btn">Sign In</button>
        </div>
      </div>

      <!-- Right Panel -->
      <div class="auth-panel right-panel">
        <div class="content">
          <h2>Create Account</h2>
          <p>Use your email for registration:</p>
          <form @submit.prevent="handleSignUp" v-if="isSignUp">
            <input type="text" placeholder="Name" v-model="signUpData.username" required />
            <input type="email" placeholder="Email" v-model="signUpData.email" required />
            <input type="password" placeholder="Password" v-model="signUpData.password" required />
            <button type="submit" class="btn">Sign Up</button>
          </form>
          <form @submit.prevent="handleLogin" v-else>
            <input type="text" placeholder="Username" v-model="loginData.username" required />
            <input type="password" placeholder="Password" v-model="loginData.password" required />
            <button type="submit" class="btn">Login</button>
          </form>
        </div>
      </div>

      <!-- Overlay -->
      <div class="overlay">
        <div class="overlay-panel overlay-left">
          <h2>Hello, Friend!</h2>
          <p>Enter your personal details and start your journey with us.</p>
          <button @click="setSignUp(true)" class="btn">Sign Up</button>
        </div>
        <div class="overlay-panel overlay-right">
          <h2>Welcome Back!</h2>
          <p>To keep connected with us, please login with your personal info.</p>
          <button @click="setSignUp(false)" class="btn">Sign In</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      isSignUp: false,
      signUpData: {
        username: "",
        email: "",
        password: "",
      },
      loginData: {
        username: "",
        password: "",
      },
    };
  },
  methods: {
    setSignUp(status) {
      this.isSignUp = status;
    },
    handleSignUp() {
      console.log("Sign Up:", this.signUpData);
    },
    handleLogin() {
      console.log("Login:", this.loginData);
    },
  },
};
</script>

<style scoped>
/* General Container */
.container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(to right, #74ebd5, #9face6);
}

/* Auth Container */
.auth-container {
  position: relative;
  width: 900px;
  height: 600px;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
  display: flex;
  transition: transform 0.6s ease-in-out;
}

/* Panels */
.auth-panel {
  flex: 1;
  padding: 50px;
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
  transition: 0.6s ease-in-out;
}

.left-panel {
  background: linear-gradient(to right, #667eea, #764ba2);
  color: white;
}

.right-panel {
  background: white;
  color: #667eea;
}

.auth-panel input {
  width: 100%;
  padding: 12px;
  margin: 10px 0;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
}

.auth-panel .btn {
  margin-top: 20px;
  padding: 10px 20px;
  border: none;
  border-radius: 25px;
  background: linear-gradient(to right, #667eea, #764ba2);
  color: white;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s ease;
}

.auth-panel .btn:hover {
  background: linear-gradient(to right, #5a67d8, #6b46c1);
}

/* Overlay */
.overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 100;
  display: flex;
  transition: transform 0.6s ease-in-out;
}

.overlay-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 50px;
  color: white;
}

.overlay-left {
  background: linear-gradient(to right, #764ba2, #667eea);
  transform: translateX(-100%);
}

.overlay-right {
  background: linear-gradient(to right, #667eea, #764ba2);
  transform: translateX(100%);
}

/* Signup Mode */
.auth-container.signup-mode .overlay {
  transform: translateX(-100%);
}

.auth-container.signup-mode .left-panel {
  transform: translateX(-100%);
}

.auth-container.signup-mode .right-panel {
  transform: translateX(-100%);
}
</style>

