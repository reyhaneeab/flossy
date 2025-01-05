<template>
  <div class="container mt-5">
    <h1 class="mb-4">My Interest List</h1>
    <div v-if="doctors && doctors.length">
      <ul class="list-group">
        <li v-for="doctor in doctors" :key="doctor" class="list-group-item">
          <h5>{{ doctor }}</h5>
        </li>
      </ul>
    </div>
    <div v-else>
      <p>No doctors in your interest list.</p>
    </div>
  </div>
</template>

<script>
import {jwtDecode} from 'jwt-decode'; 
import api from "@/api";

export default {
  data() {
    return {
      doctors: []
    };
  },
  computed: {
    userId() {
      const token = localStorage.getItem("token");
      return token ? jwtDecode(token).sub : null;
    }
  },
  methods: {
    async fetchInterestList() {
      const userId = this.userId; 
      console.log("Fetching interest list for userId:", userId); 

      if (!userId) {
        console.error("User ID is not defined.");
        return;
      }

      try {
        const response = await api.post("user/fetch", {
          userId: userId
        });
        console.log("Fetched data:", response.data); // Log the fetched data
        this.doctors = response.data.doctors; // Ensure the correct data structure is used
        console.log("Doctors list:", this.doctors); // Log the doctors list
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
  background-color: #f8f9fa;
  border: 1px solid #ddd;
  margin-bottom: 10px;
  padding: 15px;
  border-radius: 5px;
}

.list-group-item h5 {
  margin: 0;
  font-size: 18px;
  color: black; /* Updated text color to black */
}
</style>
