<template>
  <div id="container">
    <h1>Appointment Calendar</h1>
    <div v-if="loading">Loading...</div>
    <div v-else-if="error">{{ error }}</div>
    <div v-else class="calendar">
      <div class="week-selector">
        <label for="weekNumber">Week Number:</label>
        <input type="number" id="weekNumber" v-model="weekNumber" min="1" max="52">
        <button @click="fetchTimeSlots">Fetch Calendar</button>
      </div>
      <div v-for="day in days" :key="day.date" class="day">
        <h3>{{ formatDate(day.date) }}</h3>
        <div class="column-of-timeslots">
          <button
            v-for="slot in day.slots"
            :key="slot.id"
            :class="{ selected: slot.selected, booked: slot.booked }"
            @click="selectSlot(day, slot)"
            :disabled="slot.booked"
          >
            {{ formatTime(slot.startTime) }}
          </button>
        </div>
      </div>
    </div>
    <div v-if="selectedSlot" class="selected-slot">
      <h2>Selected Slot</h2>
      <p>
        Date: {{ formatDate(selectedSlot.date) }} <br />
        Time: {{ formatTime(selectedSlot.startTime) }} - {{ formatTime(selectedSlot.endTime) }}
      </p>
      <button @click="bookAppointment" :disabled="selectedSlot.booked">Book Appointment</button>
    </div>
  </div>
</template>

<script>
import { defineComponent, ref, onMounted } from "vue";
import { useStore } from 'vuex';

export default defineComponent({
  setup() {
    const store = useStore();
    const selectedSlot = ref(null);
    const days = ref([]);
    const loading = ref(false);
    const error = ref(null);
    const weekNumber = ref(getCurrentWeekNumber());

    function getCurrentWeekNumber() {
      const now = new Date();
      const start = new Date(now.getFullYear(), 0, 1);
      const diff = now - start;
      const oneWeek = 1000 * 60 * 60 * 24 * 7;
      return Math.ceil((diff + start.getTimezoneOffset() * 60 * 1000) / oneWeek);
    }

    function formatDate(dateString) {
      const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
      return new Date(dateString).toLocaleDateString(undefined, options);
    }

    function formatTime(timeString) {
      return new Date(timeString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }

    function selectSlot(day, slot) {
      if (selectedSlot.value) {
        selectedSlot.value.selected = false;
      }
      slot.selected = true;
      selectedSlot.value = { ...slot, date: day.date };
    }

    const fetchTimeSlots = async () => {
      loading.value = true;
      error.value = null;
      try {
        const response = await fetch(`${import.meta.env.VITE_APIGATEWAY_URL}/calendar/get`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            weekNumber: weekNumber.value,
            ownerId: store.state.user.id
          })
        });
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        const data = await response.json();
        handleCalendarResponse(data);
      } catch (err) {
        console.error('Error fetching time slots:', err);
        error.value = "Failed to fetch time slots. Please try again later.";
      } finally {
        loading.value = false;
      }
    };

    const handleCalendarResponse = (data) => {
      days.value = data.map(day => ({
        date: day.date,
        slots: day.timeSlots.map(slot => ({
          ...slot,
          selected: false
        }))
      }));
    };

    const bookAppointment = async () => {
      if (!selectedSlot.value || selectedSlot.value.booked) {
        return;
      }

      const bookingRequest = {
        timeSlotId: selectedSlot.value.id,
        userId: store.state.user.id,
        dentistId: selectedSlot.value.dentistId,
        startTime: selectedSlot.value.startTime,
        endTime: selectedSlot.value.endTime
      };

      try {
        const response = await fetch(`${import.meta.env.VITE_APIGATEWAY_URL}/appointment/book`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(bookingRequest)
        });
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        const data = await response.json();
        handleBookingResponse(data);
      } catch (err) {
        console.error('Error booking appointment:', err);
        alert("Failed to book appointment. Please try again later.");
      }
    };

    const handleBookingResponse = (response) => {
      if (response.success) {
        // Update the local state to reflect the booked appointment
        days.value.forEach(day => {
          day.slots.forEach(slot => {
            if (slot.id === response.timeSlotId) {
              slot.booked = true;
            }
          });
        });
        selectedSlot.value.booked = true;
        alert("Appointment booked successfully!");
      } else {
        alert("Failed to book appointment: " + response.message);
      }
    };

    // Lifecycle hooks
    onMounted(fetchTimeSlots);

    return {
      days,
      selectedSlot,
      loading,
      error,
      formatDate,
      formatTime,
      selectSlot,
      weekNumber,
      fetchTimeSlots,
      bookAppointment
    };
  }
});
</script>

<style scoped>
#container {
  font-family: "Avenir", Helvetica, Arial, sans-serif;
  text-align: center;
  margin: 20px;
}

.calendar {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 20px;
}

.week-selector {
  width: 100%;
  margin-bottom: 20px;
}

.day {
  border: 1px solid #ddd;
  padding: 20px;
  border-radius: 5px;
  background: #f9f9f9;
  width: 300px;
}

h3 {
  font-size: 1.2rem;
  margin-bottom: 10px;
}

.column-of-timeslots {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

button {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 3px;
  background: #fff;
  cursor: pointer;
  transition: 0.3s;
  font-size: 14px;
}

button:hover:not(:disabled) {
  background: #eef;
}

button.selected {
  background: #add8e6;
  border-color: #00f;
  color: #000;
}

button.booked {
  background: #f0f0f0;
  color: #888;
  cursor: not-allowed;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.selected-slot {
  margin-top: 20px;
}
</style>