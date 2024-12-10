<template>
  <div id="container">
    <h1>Appointment calendar</h1>
    <div class="calendar">
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
            weekNumber: "1",
            ownerId: "675478941fc975f2914c2650"
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
</style>
