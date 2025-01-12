<template>
  <div class="container mt-5">
    <h1 class="mb-4">My Interest List</h1>
    <div v-if="loading" class="loading">Loading...</div>
    <div v-else>
      <div v-if="timeSlots && timeSlots.length">
        <div class="row">
          <div v-for="slot in timeSlots" :key="slot.id" class="col-md-6 col-lg-4 mb-4">
            <div class="card calendar-card">
              <div class="card-header">
                <h3 class="card-title">{{ formatDate(slot.startTime) }}</h3>
              </div>
              <div class="card-body">
                <div class="d-flex flex-column">
                  <div class="time-slot">
                    <p>{{ formatTime(slot.startTime) }} - {{ formatTime(slot.endTime) }}</p>
                    <button @click="removeTimeSlot(slot)" class="btn btn-danger btn-sm">Remove</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else>
        <p>No time slots in your interest list.</p>
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
      timeSlots: [],
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
    formatDate(dateString) {
      const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
      return new Date(dateString).toLocaleDateString(undefined, options);
    },
    formatTime(dateString) {
      return new Date(dateString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    },
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
        this.timeSlots = response.data.timeSlots; // Ensure the correct data structure is used
        console.log("Time slots list:", this.timeSlots); // Log the time slots list
        localStorage.setItem("timeSlots", JSON.stringify(this.timeSlots)); // Save to localStorage
      } catch (error) {
        console.error("Error fetching interest list:", error);
      } finally {
        this.loading = false;
      }
    },
    async removeTimeSlot(slot) {
      const userId = this.userId;
      console.log("Removing time slot:", slot, "for userId:", userId);

      if (!userId) {
        console.error("User ID is not defined.");
        return;
      }

      const confirmation = confirm(`Are you sure you want to remove this time slot from your interest list?`);
      if (!confirmation) {
        return;
      }

      try {
        await api.post("user/delete", {
          userId: userId,
          timeSlots: [slot]
        });
        console.log("Time slot removed:", slot);
        this.timeSlots = this.timeSlots.filter(s => s.id !== slot.id);
        localStorage.setItem("timeSlots", JSON.stringify(this.timeSlots)); 

        alert(`Time slot has been removed from your interest list.`);

        if (!this.timeSlots.length) {
          localStorage.removeItem("timeSlots");
        }
      } catch (error) {
        console.error("Error removing time slot:", error);
      }
    }
  },
  async mounted() {
    const savedTimeSlots = localStorage.getItem("timeSlots");
    if (savedTimeSlots) {
      this.timeSlots = JSON.parse(savedTimeSlots);
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

.calendar-card {
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.calendar-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.card-header {
  background-color: #f8f9fa;
  border-bottom: 2px solid #007bff;
}

.card-title {
  color: #007bff;
  font-size: 1.2rem;
  margin-bottom: 0;
}

.time-slot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.time-slot p {
  margin: 0;
  font-size: 16px;
}

.loading {
  text-align: center;
  font-size: 20px;
  color: white;
}
</style>
