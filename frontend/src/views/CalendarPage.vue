<template>
  <!-- Container with top margin for overall layout -->
  <div class="container mt-4">
    <!-- Centered heading with bottom margin -->
    <h1 class="text-center mb-4">Appointment Calendar</h1>
    <!-- Bootstrap row for grid layout -->
    <div class="row">
      <!--
           - On medium screens (md) and up, each card takes 6 columns (half width)
           - On large screens (lg) and up, each card takes 4 columns (one-third width)
           - mb-4 adds bottom margin for vertical spacing between cards -->
      <div v-for="day in days" :key="day.date" class="col-md-6 col-lg-4 mb-4">
        <!-- Card component for each day -->
        <div class="card calendar-card">
          <div class="card-header">
            <h3 class="card-title">{{ formatDate(day.date) }}</h3>
          </div>
          <div class="card-body">
            <!-- Flexbox container for vertical alignment of buttons -->
            <div class="d-flex flex-column">
              <!-- Time slot buttons -->
              <button
                  v-for="slot in day.slots"
                  :key="slot.time"
                  class="btn btn-outline-primary mb-2 time-slot"
                  :class="{ 'btn-primary text-white': slot.selected, 'disabled': slot.booked }"
                  @click="selectSlot(day, slot)"
                  :disabled="slot.booked"
              >
                {{ slot.time }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- Selected slot information, shown conditionally -->
    <div v-if="selectedSlot" class="mt-4 selected-slot-info">
      <h2 class="text-center">Selected Slot</h2>
      <p class="text-center">
        Date: {{ formatDate(selectedSlot.date) }} <br />
        Time: {{ selectedSlot.time }}
      </p>
    </div>
  </div>
</template>


<script>
import { defineComponent, ref, onMounted } from "vue";

export default defineComponent({
  setup() {
    const selectedSlot = ref(null);
    const days = ref([]);
    const URL = `http://localhost:8091/api/calendar/get`

    const fetchTimeSlots = async () => {
      try {
        const response = await fetch(URL, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            weekNumber: "50",
            ownerId: "674a2c3e3881dd327be177fd"
          })
        })

        const data = await response.json();

        // Group the time slots by day and create a proper structure for `days`
        const dayMap = {};

        data.timeSlots.forEach(slot => {
          const date = slot.date;
          if (!dayMap[date]) {
            dayMap[date] = {
              date: date,
              slots: []
            };
          }

          // Push the slot details into the correct day
          dayMap[date].slots.push({
            id: slot.id,
            time: `${new Date(slot.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} - ${new Date(slot.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`,
            booked: slot.booked,
            selected: false,
          });
        });

        // Assign the grouped slots to the days array
        days.value = Object.values(dayMap);

      } catch (error) {
        console.error("Failed to fetch time slots:", error);
      }
    };

    const selectSlot = (day, slot) => {
      // Deselect any previously selected slot
      days.value.forEach(day => {
        day.slots.forEach(slot => {
          slot.selected = false;
        });
      });
      // Select the clicked slot
      slot.selected = true;
      selectedSlot.value = slot;
    };

    const formatDate = (dateString) => {
      const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
      return new Date(dateString).toLocaleDateString(undefined, options);
    };

    onMounted(() => {
      fetchTimeSlots();
    });

    return {
      days,
      selectedSlot,
      formatDate,
      selectSlot,
    };
  },
});
</script>

<style scoped>
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
  transition: all 0.2s ease;
}

.time-slot:hover:not(.disabled) {
  transform: scale(1.05);
}

.time-slot.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.selected-slot-info {
  background-color: #e9ecef;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

h1, h2 {
  color: #343a40;
}

@media (max-width: 768px) {
  .calendar-card {
    margin-bottom: 20px;
  }
}
</style>
