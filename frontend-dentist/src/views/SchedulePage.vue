<template>
  <div id="schedule-page">
    <h1>Your Schedule</h1>
    <div v-for="(appointment, index) in appointments" :key="index" class="appointment-item">
      <p><strong>Date:</strong> {{ appointment.date }}</p>
      <p><strong>Time:</strong> {{ appointment.time }}</p>
      <p><strong>Patient Name:</strong> {{ appointment.patientName }}</p>
      <button @click="cancelAppointment(index)">Cancel Appointment</button>
    </div>
  </div>
</template>

<script>
import apiClient from "@/api";

export default {
  name: 'SchedulePage',
  data() {
    return {
      appointments: [
        { date: '2024-12-16', time: '10:00 AM - 11:00 AM', patientName: 'John Doe' },
        { date: '2024-12-17', time: '11:00 AM - 12:00 PM', patientName: 'Jane Smith' }
      ],
      dentistId: localStorage.getItem("id")
    };
  },
  methods: {
    cancelAppointment(index) {
      // Placeholder logic for canceling an appointment
      alert(`Appointment on ${this.appointments[index].date} at ${this.appointments[index].time} has been canceled.`);
      this.appointments.splice(index, 1);
    }
  },
  mounted() {
    apiClient.get(`calendar/${this.dentistId}`)
  }
};
</script>

<style>
#schedule-page {
  padding: 20px;
}
.appointment-item {
  background-color: #f9f9f9;
  margin: 10px 0;
  padding: 15px;
  border-radius: 5px;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);
}
button {
  margin-top: 10px;
  padding: 8px 15px;
  background-color: #ff4d4d;
  color: white;
  border: none;
  border-radius: 3px;
  cursor: pointer;
}
button:hover {
  background-color: #ff1a1a;
}
</style>
