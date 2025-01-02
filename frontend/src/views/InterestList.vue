<template>
  <div class="container mt-5">
    <h1 class="mb-4">My Interest List</h1>
    <div v-if="timeSlots.length">
      <ul class="list-group">
        <li v-for="slot in timeSlots" :key="slot.id" class="list-group-item">
          <h5>{{ slot.time }}</h5>
          <p>{{ formatDate(slot.date) }}</p>
        </li>
      </ul>
    </div>
    <div v-else>
      <p>You have no time slots in your interest list.</p>
    </div>
  </div>
</template>

<script>
import {jwtDecode} from "jwt-decode";
import api from "@/api";

export default {
  data() {
    return {
      timeSlots: []
    };
  },
  methods: {
    async fetchInterestList() {
      try {
        const token = localStorage.getItem('token');
        if (!token) throw new Error("User is not logged in.");

        const { sub: userId } = jwtDecode(token);
        const response = await api.post('user/interestList', { userId });
        this.timeSlots = response.data.timeSlots;
      } catch (error) {
        console.error('Error fetching interest list:', error.message, error.response?.data || error);
        if (error.response?.status === 408) {
          alert("The server is taking too long to respond. Please try again later.");
        }
      }
    },
    formatDate(dateString) {
      const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
      return new Date(dateString).toLocaleDateString(undefined, options);
    },
  },
  async mounted() {
    await this.fetchInterestList();
  },
};
</script>

<style scoped>
.container {
  max-width: 800px;
  margin: auto;
}

h1 {
  color: #333;
}

.list-group-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-group-item h5 {
  margin: 0;
}

.list-group-item p {
  margin: 0;
  color: #666;
}
</style>
