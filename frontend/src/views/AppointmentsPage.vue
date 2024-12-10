<template>
  <div>
    <div class="appointment-box">
      <h1>Your Appointments</h1>
      <div v-if="backendMessage" class="backend-response">
        <p>{{ backendMessage }}</p>
      </div>
      <div v-for="day in days" :key="day.date" class="day-container">
        <h2>{{ formatDate(day.date) }}</h2>
        <ul>
          <li v-for="slot in day.slots" :key="slot.id" :class="{ selected: slot.selected }">
            <button @click="selectSlot(day, slot)">
              {{ slot.time }} - {{ slot.booked ? "Booked" : "Available" }}
            </button>
          </li>
        </ul>
      </div>
      <div v-if="showAlert" class="alert-box">
        <p>Are you sure you want to cancel this appointment?</p>
        <button @click="confirmCancel">Yes</button>
        <button @click="closeAlert">No</button>
      </div>
    </div>

  </div>
</template>

<script>
import { defineComponent, ref, onMounted } from "vue";
//import {useRouter} from "vue-router";

export default defineComponent({
  setup() {
    const selectedSlot = ref(null);
    const days = ref([]);
    const showAlert = ref(false);
    const backendMessage = ref(null);

    const fetchURL = `http://localhost:8091/api/calendar/get`;
    const cancelURL = `http://localhost:8091/api/timeSlot/cancel`;
    //const router = useRouter()

    const fetchTimeSlots = async () => {
      try {
        const response = await fetch(fetchURL, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            weekNumber: "1",
            ownerId: "675478941fc975f2914c2650",
          }),
        });

        const data = await response.json();

        const dayMap = {};
        data.timeSlots.forEach(slot => {
          const date = slot.date;
          if (!dayMap[date]) {
            dayMap[date] = {
              date: date,
              slots: [],
            };
          }
          dayMap[date].slots.push({
            id: slot.id,
            time: `${new Date(slot.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} - ${new Date(slot.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`,
            booked: slot.booked,
            selected: false,
          });
        });

        days.value = Object.values(dayMap);
      } catch (error) {
        console.error("Failed to fetch time slots:", error);
      }
    };

    const selectSlot = (day, slot) => {
      // Deselect any previously selected slot
      days.value.forEach(day => {
        day.slots.forEach(slot =>
        {slot.selected = false});
      });

      // Mark the selected slot
      slot.selected = true;
      selectedSlot.value = slot;

      // Show cancel alert after slot selection
      showAlert.value = true;
    };

    const confirmCancel = async () => {
      if (selectedSlot.value) {
        try {
          const response = await fetch(cancelURL, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify({
              slotId: selectedSlot.value.id, // Pass the selected slot ID to the backend
            }),
          });

          if (response.ok) {
            backendMessage.value = `Appointment at ${selectedSlot.value.time} has been canceled.`;
            selectedSlot.value = null;

            // Refresh the time slots
            fetchTimeSlots();
          } else {
            const error = await response.json();
            backendMessage.value = `Failed to cancel appointment: ${error.message}`;
          }
        } catch (error) {
          console.error("Failed to cancel the appointment:", error);
          backendMessage.value = "An error occurred while canceling the appointment.";
        }
      }
      showAlert.value = false;
    };

    const formatDate = dateString => {
      const options = { weekday: "long", year: "numeric", month: "long", day: "numeric" };
      return new Date(dateString).toLocaleDateString(undefined, options);
    };

    const closeAlert = () => {
      showAlert.value = false;
    };

    onMounted(() => {
      fetchTimeSlots();
    });

    return {
      days,
      selectedSlot,
      showAlert,
      backendMessage,
      formatDate,
      selectSlot,
      closeAlert,
      confirmCancel,
    };
  },
});
</script>

<style scoped>
.appointment-box {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
  margin: 20px auto;
  max-width: 400px;
  text-align: center;
  background-color: #f9f9f9;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

h1 {
  color: #333;
  font-size: 24px;
  margin: 0 0 20px 0;
}

button {
  padding: 10px 20px;
  font-size: 16px;
  background-color: #007bff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #0056b3;
}

.alert-box {
  margin-top: 20px;
  padding: 15px;
  border: 1px solid #f5c6cb;
  background-color: #f8d7da;
  border-radius: 4px;
  text-align: center;
}

.alert-box p {
  margin: 0 0 10px;
  color: #721c24;
}

.alert-box button {
  margin: 5px;
  padding: 8px 16px;
  font-size: 14px;
}

.alert-box button:first-child {
  background-color: #dc3545;
  color: #fff;
}

.alert-box button:first-child:hover {
  background-color: #c82333;
}

.alert-box button:last-child {
  background-color: #6c757d;
  color: #fff;
}

.alert-box button:last-child:hover {
  background-color: #5a6268;
}

.backend-response {
  margin-top: 20px;
  padding: 10px;
  background-color: #e9f7ef;
  border: 1px solid #d4edda;
  border-radius: 4px;
  color: #155724;
  text-align: center;
}

.day-container {
  margin: 20px 0;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.day-container h2 {
  font-size: 20px;
  margin-bottom: 10px;
}

.day-container ul {
  list-style: none;
  padding: 0;
}

.day-container li {
  margin-bottom: 10px;
}

.day-container li button {
  width: 100%;
}

.day-container li.selected button {
  background-color: #28a745;
  color: white;
}
</style>
