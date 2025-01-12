<template>
  <div class="container mt-5">
    <h1 class="mb-4">My Interest List</h1>
    <div v-if="loading" class="loading">Loading...</div>
    <div v-else>
      <div v-if="dentists && dentists.length">
        <div class="row">
          <div v-for="dentist in dentists" :key="dentist.id" class="col-md-6 col-lg-4 mb-4">
            <div class="card calendar-card">
              <div class="card-header">
                <h3 class="card-title">{{ dentist.name }}</h3>
              </div>
              <div class="card-body">
                <div class="d-flex flex-column">
                  <div class="dentist-info">
                    <p>{{ dentist.address }}</p>
                    <button @click="removeDentist(dentist)" class="btn btn-danger btn-sm">Remove</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else>
        <p>No dentists in your interest list.</p>
      </div>
    </div>
  </div>
</template>

<script>
import { jwtDecode } from 'jwt-decode';
import api from "@/api";

export default {
  data() {
    return {
      dentists: [],
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
        console.log("Fetched data:", response.data);
        this.dentists = response.data.dentists;
        console.log("Dentist list:", this.dentists);
        localStorage.setItem("dentists", JSON.stringify(this.dentists));
      } catch (error) {
        console.error("Error fetching interest list:", error);
      } finally {
        this.loading = false;
      }
    },
    async removeDentist(dentist) {
      const userId = this.userId;
      console.log("Removing dentist:", dentist, "for userId:", userId);

      if (!userId) {
        console.error("User ID is not defined.");
        return;
      }

      const confirmation = confirm(`Are you sure you want to remove this dentist from your interest list?`);
      if (!confirmation) {
        return;
      }

      try {
        await api.post("user/delete", {
          userId: userId,
          dentistId: dentist.id
        });
        console.log("Dentist removed:", dentist);
        this.dentists = this.dentists.filter(d => d.id !== dentist.id);
        localStorage.setItem("dentists", JSON.stringify(this.dentists));

        alert(`Dentist has been removed from your interest list.`);

        if (!this.dentists.length) {
          localStorage.removeItem("dentists");
        }
      } catch (error) {
        console.error("Error removing dentist:", error);
      }
    }
  },
  async mounted() {
    const savedDentists = localStorage.getItem("dentists");
    if (savedDentists) {
      this.dentists = JSON.parse(savedDentists);
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

.dentist-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.dentist-info p {
  margin: 0;
  font-size: 16px;
}

.loading {
  text-align: center;
  font-size: 20px;
  color: white;
}
</style>
