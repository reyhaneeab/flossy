<template>
  <div class="container mt-5">
    <h1 class="mb-4">My Interest List</h1>
    <div v-if="doctors && doctors.length">
      <ul class="list-group">
        <li v-for="doctor in doctors" :key="doctor.id" class="list-group-item">
          <h5>{{ doctor.name }}</h5>
          <p>{{ doctor.specialty }}</p>
        </li>
      </ul>
    </div>
    <div v-else>
      <p>No doctors in your interest list.</p>
    </div>
  </div>
</template>

<script>
import { jwtDecode } from 'jwt-decode'; 

export default {
  data() {
    return {
      doctors: []
    };
  },
  computed: {
    userId() {
      const token = localStorage.getItem("token");
      return token ? jwtDecode(token).sub : null; // Decode token to get userId
    }
  },
  methods: {
    async fetchInterestList() {
      const userId = this.userId; // Make sure this is correctly set
      console.log("Fetching interest list for userId:", userId); // Check userId here

      if (!userId) {
        console.error("User ID is not defined.");
        return; // Return early if the userId is not set
      }

      try {
        const response = await fetch("http://localhost:8091/api/users/fetch", {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({ userId })
        });
        const data = await response.json();
        console.log(data);
      } catch (error) {
        console.error("Error fetching interest list:", error);
      }
    }
  },
  async mounted() {
    this.fetchInterestList();
  },
};
</script>

<style scoped>
.container {
  max-width: 800px;
  margin: auto;
}

h1 {
  color: white;
  font-weight: bold;
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
