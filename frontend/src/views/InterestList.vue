<template>
  <div class="container mt-5">
    <h1 class="mb-4">My Interest List</h1>
    <div v-if="loading" class="loading">Loading...</div>
    <div v-else>
      <div v-if="doctors && doctors.length">
        <ul class="list-group">
          <li v-for="doctor in doctors" :key="doctor" class="list-group-item d-flex justify-content-between align-items-center">
            <h5>{{ doctor }}</h5>
            <button @click="removeDoctor(doctor)" class="btn btn-danger btn-sm">Remove</button>
          </li>
        </ul>
      </div>
      <div v-else>
        <p>No doctors in your interest list.</p>
      </div>
    </div>
  </div>
</template>

<script>
import {jwtDecode} from 'jwt-decode'; 
import api from "@/api";

export default {
  data() {
    return {
      doctors: [],
      loading: false
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
        this.loading = false;
        return;
      }

      try {
        const response = await api.post("user/fetch", {
          userId: userId
        });
        console.log("Fetched data:", response.data); // Log the fetched data
        this.doctors = response.data.doctors; // Ensure the correct data structure is used
        console.log("Doctors list:", this.doctors); // Log the doctors list
        localStorage.setItem("doctors", JSON.stringify(this.doctors)); // Save to localStorage
      } catch (error) {
        console.error("Error fetching interest list:", error);
      } finally {
        this.loading = false;
      }
    },
    async removeDoctor(doctor) {
      const userId = this.userId;
      console.log("Removing doctor:", doctor, "for userId:", userId);

      if (!userId) {
        console.error("User ID is not defined.");
        return;
      }

      const confirmation = confirm(`Are you sure you want to remove Dr. ${doctor} from your interest list?`);
      if (!confirmation) {
        return;
      }

      try {
        await api.post("user/delete", {
          userId: userId,
          doctor: doctor
        });
        console.log("Doctor removed:", doctor);
        this.doctors = this.doctors.filter(d => d !== doctor);
        localStorage.setItem("doctors", JSON.stringify(this.doctors)); 

        alert(`Dr. ${doctor} has been removed from your interest list.`);

        if (!this.doctors.length) {
          localStorage.removeItem("doctors");
        }
      } catch (error) {
        console.error("Error removing doctor:", error);
      }
    }
  },
  async mounted() {
    const savedDoctors = localStorage.getItem("doctors");
    if (savedDoctors) {
      this.doctors = JSON.parse(savedDoctors);
    } else {
      this.fetchInterestList();
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

.loading {
  text-align: center;
  font-size: 20px;
  color: white;
}
</style>
