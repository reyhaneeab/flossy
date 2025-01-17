<template>
  <div class="container">
    <div class="title">Registration</div>
    <div class="content">
      <form @submit.prevent="handleSignup">
        <div class="user-details">
          <div class="input-box">
            <span class="details">First Name</span>
            <input type="text" v-model="firstName" placeholder="Enter your first name" required>
          </div>
          <div class="input-box">
            <span class="details">Last Name</span>
            <input type="text" v-model="lastName" placeholder="Enter your last name" required>
          </div>
          <div class="input-box">
            <span class="details">Email</span>
            <input type="email" v-model="email" placeholder="Enter your email" required>
          </div>
          <div class="input-box">
            <span class="details">Phone Number</span>
            <input type="number" v-model="phone" placeholder="Enter your phone number" required>
          </div>
          <div class="input-box">
            <span class="details">Password</span>
            <input type="password" v-model="password" placeholder="Enter your password" required>
          </div>
          <div class="input-box">
            <span class="details">Confirm Password</span>
            <input type="password" v-model="confirmPassword" placeholder="Confirm your password" required>
          </div>
        </div>
        <div v-if="error" class="error-message">{{error}}</div>
        <div class="button">
          <input type="submit" value="Register">
        </div>
      </form>
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
        if(response.status === 200){
          console.log('User created successfully', response.data)
          alert("User created successfully")
          await router.push('/login')
        }
        else{
          alert("Email is already in use")
        }
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
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@200;300;400;500;600;700&display=swap');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Poppins', sans-serif;
}

body {
  height: 100vh; /* Ensure body takes full height */
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #71b7e6, #9b59b6);
}

.container {
  max-width: 1000px;
  width: 100%; /* Ensure container takes full width */
  background-color: #fff;
  padding: 25px 30px;
  box-shadow: 0 5px 10px rgba(0, 0, 0, 0.15);
  display: flex; /* Ensure flex display */
  flex-direction: column;
  align-items: center; 
  color: black; /* Set text color to black */
  margin: auto; /* Center the container */
}

.container .title {
  font-size: 25px;
  font-weight: 500;
  position: relative;
  color: black; /* Set text color to black */
}

.container .title::before {
  content: "";
  position: absolute;
  left: 0;
  bottom: 0;
  height: 3px;
  width: 30px;
  border-radius: 5px;
  background: linear-gradient(135deg, #71b7e6, #9b59b6);
}

.content form .user-details {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  margin: 20px 0 12px 0;
}

form .user-details .input-box {
  margin-bottom: 15px;
  width: calc(100% / 2 - 20px);
}

form .input-box span.details {
  display: block;
  font-weight: 500;
  margin-bottom: 5px;
}

.user-details .input-box input {
  height: 45px;
  width: 100%;
  outline: none;
  font-size: 16px;
  border-radius: 5px;
  padding-left: 15px;
  border: 1px solid #ccc;
  border-bottom-width: 2px;
  transition: all 0.3s ease;
  color: black; /* Set text color to black */
}

.user-details .input-box input:focus,
.user-details .input-box input:valid {
  border-color: #9b59b6;
}

form .button {
  height: 45px;
  margin: 35px 0;
}

form .button input {
  height: 100%;
  width: 100%;
  border-radius: 5px;
  border: none;
  color: #fff;
  font-size: 18px;
  font-weight: 500;
  letter-spacing: 1px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #71b7e6, #9b59b6);
}

form .button input:hover {
  background: linear-gradient(-135deg, #71b7e6, #9b59b6);
}

.error-message {
  color: #dc3545;
  font-size: 0.875rem;
  margin-top: -0.5rem;
  margin-bottom: 1rem;
}

@media(max-width: 584px) {
  .container {
    max-width: 100%;
  }

  form .user-details .input-box {
    margin-bottom: 15px;
    width: 100%;
  }

  .content form .user-details {
    max-height: 300px;
    overflow-y: scroll;
  }

  .user-details::-webkit-scrollbar {
    width: 5px;
  }
}

@media(max-width: 459px) {
  .container .content .category {
    flex-direction: column;
  }
}
</style>


