<template>
  <div class="container mt-4">
    <h1 class="text-center mb-4">Appointment Calendar</h1>
    <div class="row">
      <div v-for="day in days" :key="day.date" class="col-md-6 col-lg-4 mb-4">
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">{{ formatDate(day.date) }}</h3>
          </div>
          <div class="card-body">
            <div class="d-flex flex-column">
              <button
                v-for="slot in day.slots"
                :key="slot.time"
                class="btn btn-outline-primary mb-2"
                :class="{ 'btn-primary text-white': slot.selected }"
                @click="selectSlot(day, slot)"
              >
                {{ slot.time }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="selectedSlot" class="mt-4">
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

</style>
