<template>
  <div>
    <div class="appointment-box">
      <h1>Your Appointments</h1>
      <button @click="showCancelAlert">Cancel Appointment</button>
      <div v-if="showAlert" class="alert-box">
        <p>Are you sure you want to cancel this appointment?</p>
        <button @click="confirmCancel">Yes</button>
        <button @click="closeAlert">No</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const showAlert = ref(false);

const showCancelAlert = () => {
  showAlert.value = true;
};

const closeAlert = () => {
  showAlert.value = false;
};

const confirmCancel = () => {
  alert('Appointment canceled.');
  closeAlert();
};
</script>

<style scoped>
.appointment-box {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
  margin: 20px auto;
  max-width: 400px;
  text-align: center;
  background-color: #f9f9f9;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

h1 {
  color: #333;
  font-size: 24px;
  margin: 0 0 20px 0;
}

button {
  padding: 10px 20px;
  font-size: 16px;
  background-color: #007bff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #0056b3;
}

.alert-box {
  margin-top: 20px;
  padding: 15px;
  border: 1px solid #f5c6cb;
  background-color: #f8d7da;
  border-radius: 4px;
  text-align: center;
}

.alert-box p {
  margin: 0 0 10px;
  color: #721c24;
}

.alert-box button {
  margin: 5px;
  padding: 8px 16px;
  font-size: 14px;
}

.alert-box button:first-child {
  background-color: #dc3545;
  color: #fff;
}

.alert-box button:first-child:hover {
  background-color: #c82333;
}

.alert-box button:last-child {
  background-color: #6c757d;
  color: #fff;
}

.alert-box button:last-child:hover {
  background-color: #5a6268;
}
</style>


