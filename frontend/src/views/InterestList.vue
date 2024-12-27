<template>
  <div class="container mt-5">
    <h1 class="mb-4">My Interest List</h1>
    <div v-if="doctors.length">
      <ul class="list-group">
        <li v-for="doctor in doctors" :key="doctor.id" class="list-group-item">
          <h5>{{ doctor.name }}</h5>
          <p>{{ doctor.specialty }}</p>
        </li>
      </ul>
    </div>
    <div v-else>
      <p>You have no doctors in your interest list.</p>
    </div>
  </div>
</template>

<script>
import { jwtDecode } from "jwt-decode"; // Ensure correct import

export default {
  name: "InterestList",
  data() {
    return {
      doctors: [],
    };
  },
  async created() {
    try {
      const userId = jwtDecode(localStorage.getItem("token")).sub; // Ensure getUserId function is called correctly
      const response = await fetch(`/api/user/interestList/${userId}`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
      });
      const data = await response.json();
      this.doctors = data;
    } catch (error) {
      console.error("Error fetching interest list:", error);
    }
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
