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
import jwt_decode from "jwt-decode"; // Correct import statement
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
        const userId = jwt_decode(token).sub; // Decode the token to get the user ID
        const response = await api.post('user/interestList', {
          userId: userId
        });
        this.timeSlots = response.data;
      } catch (error) {
        console.error('Error fetching interest list:', error);
      }
    },
    formatDate(dateString) {
      const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
      return new Date(dateString).toLocaleDateString(undefined, options);
    }
  },
  async mounted() {
    await this.fetchInterestList();
  }
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
