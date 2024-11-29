<template>
  <div id="app">
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
import { defineComponent, ref } from "vue";

export default defineComponent({
  setup() {
    const selectedSlot = ref(null);

    const days = ref(
      Array.from({ length: 5 }, (_, i) => {
        const date = new Date();
        date.setDate(date.getDate() + i);
        return {
          date,
          slots: [
            { time: "9:00 AM", selected: false },
            { time: "10:00 AM", selected: false },
            { time: "11:00 AM", selected: false },
            { time: "1:00 PM", selected: false },
            { time: "2:00 PM", selected: false },
          ],
        };
      })
    );

    const selectSlot = (day, slot) => {
      // Deselect all slots
      days.value.forEach((d) =>
        d.slots.forEach((s) => {
          s.selected = false;
        })
      );

      // Select the clicked slot
      slot.selected = true;

      // Update the selected slot
      selectedSlot.value = {
        date: day.date,
        time: slot.time,
      };
    };

    const formatDate = (date) => {
      const options = { weekday: "short", month: "short", day: "numeric" };
      return new Intl.DateTimeFormat("en-US", options).format(date);
    };

    return {
      days,
      selectedSlot,
      selectSlot,
      formatDate,
    };
  },
});
</script>

<style scoped>
#app {
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
