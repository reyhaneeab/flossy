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
        this.doctors = response.data;
        console.log(this.doctors);
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
