<template>
  <div id="container">
    <h1>Appointment calendar</h1>
    <div v-if="loading">Loading...</div>
    <div v-else-if="error">{{ error }}</div>
    <div v-else class="calendar">
      <div>
        <label for="weekNumber">Week Number:</label>
        <input type="number" id="weekNumber" v-model="weekNumber" min="1" max="52">
        <button @click="fetchTimeSlots">Fetch Calendar</button>
      </div>
      <div v-for="day in days" :key="day.date" class="day">
        <h3>{{ formatDate(day.date) }}</h3>
        <div class="column-of-timeslots">
          <button
              v-for="slot in day.slots"
              :key="slot.time"
              :class="{ selected: slot.selected }"
              @click="selectSlot(day, slot)"
          >
            {{ slot.time }}
          </button>
        </div>
      </div>
    </div>
    <div v-if="selectedSlot">
      <h2>Selected Slot</h2>
      <p>
        Date: {{ formatDate(selectedSlot.date) }} <br />
        Time: {{ selectedSlot.time }}
      </p>
      <button @click="bookAppointment" :disabled="selectedSlot.booked">Book Appointment</button>
    </div>
  </div>
</template>

<script>
import { defineComponent, ref, onMounted, onUnmounted } from "vue";
import mqtt from 'mqtt';
import { useStore } from 'vuex';

export default defineComponent({
  setup() {
    // Reactive references for component state
    const store = useStore();
    const selectedSlot = ref(null);
    const days = ref([]);
    const loading = ref(false);
    const error = ref(null);
    const client = ref(null);
    const weekNumber = ref(getCurrentWeekNumber());
// MQTT configuration
    const MQTT_BROKER_URL = 'ws://localhost:1883';
    const CALENDAR_GET_TOPIC = 'calendar/get';
    const CALENDAR_GET_RESPONSE = 'calendar/get/response';
    const APPOINTMENT_BOOK_TOPIC = 'appointment/book';
    const APPOINTMENT_BOOK_RESPONSE = 'appointment/book/response';

    const getCurrentWeekNumber = () => {
      const now = new Date();
      const start = new Date(now.getFullYear(), 0, 1);
      const diff = now - start;
      const oneWeek = 1000 * 60 * 60 * 24 * 7;
      return Math.ceil((diff + start.getTimezoneOffset() * 60 * 1000) / oneWeek);
    };

    const connectMqtt = () => {
// Create and store MQTT client instance
      client.value = mqtt.connect(MQTT_BROKER_URL);
// Set up event handlers for MQTT client
      client.value.on('connect', () => {
        console.log('Connected to MQTT broker');
        client.value.subscribe(CALENDAR_GET_RESPONSE);
        client.value.subscribe(APPOINTMENT_BOOK_RESPONSE);

// Fetch time slots once connected
        fetchTimeSlots();
      });
// Handle incoming MQTT messages
      client.value.on('message', (topic, message) => {
        if (topic === CALENDAR_GET_RESPONSE) {
// Parse and process the calendar data
          handleCalendarResponse(JSON.parse(message.toString()));
        }else if (topic === APPOINTMENT_BOOK_RESPONSE) {
          handleBookingResponse(JSON.parse(message.toString()));
        }
      });

      client.value.on('error', (err) => {
        console.error('MQTT Error:', err);
        error.value = "Failed to connect to the server. Please try again later.";
      });
    };

    const fetchTimeSlots = () => {
      loading.value = true;
      error.value = null;
      const request = {
        weekNumber: weekNumber.value,
        ownerId: store.state.user.id
      };
      client.value.publish(CALENDAR_GET_TOPIC, JSON.stringify(request));
    };

    const handleCalendarResponse = (data) => {
      const dayMap = {};

      data.timeSlots.forEach(slot => {
        const date = slot.date;
        if (!dayMap[date]) {
          dayMap[date] = {
            date: date,
            slots: []
          };
        }

        dayMap[date].slots.push({
          id: slot.id,
          time: `${new Date(slot.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} - ${new Date(slot.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`,
          booked: slot.booked,
          selected: false,
          startTime: slot.startTime,
          endTime: slot.endTime
        });
      });

      days.value = Object.values(dayMap);
      loading.value = false;
    };

    const selectSlot = (day, slot) => {
      days.value.forEach(d => {
        d.slots.forEach(s => {
          s.selected = false;
        });
      });
      slot.selected = true;
      selectedSlot.value = { ...slot, date: day.date };
    };

    const formatDate = (dateString) => {
      const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
      return new Date(dateString).toLocaleDateString(undefined, options);
    };
    const bookAppointment = () => {
      if (!selectedSlot.value || selectedSlot.value.booked) {
        return;
      }

      const bookingRequest = {
        timeSlotId: selectedSlot.value.id,
        userId: store.state.user.id,
        dentistId: store.state.user.id, // Assuming the logged-in user is the dentist
        startTime: selectedSlot.value.startTime,
        endTime: selectedSlot.value.endTime
      };

      client.value.publish(APPOINTMENT_BOOK_TOPIC, JSON.stringify(bookingRequest));
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
    onMounted(connectMqtt);

    onUnmounted(() => {
      if (client.value) {
        client.value.end();
      }
    });
// Return reactive references and methods for use in the template
    return {
      days,
      selectedSlot,
      loading,
      error,
      formatDate,
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
  justify-content: center;
  gap: 20px;
}

.day {
  border: 1px solid #ddd;
  padding: 20px;
  border-radius: 5px;
  background: #f9f9f9;
  width: 400px;
}

h3 {
  font-size: 1.5rem;
  margin-bottom: 10px;
}

.column-of-timeslots {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

button {
  padding: 30px 50px;
  border: 1px solid #ddd;
  border-radius: 3px;
  background: #fff;
  cursor: pointer;
  transition: 0.3s;
  font-size: 15px;
  font-weight: bold;
}

button:hover {
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
</style>
