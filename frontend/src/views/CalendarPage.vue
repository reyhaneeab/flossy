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
      <h2 class="text-center">Selected Time Slot</h2>
      <p class="text-center">
        Date: {{ formatDate(selectedSlot.date) }} <br />
        Time: {{ selectedSlot.time }}
      </p>
      <div @click="book" id="bookButton">BOOK APPOINTMENT</div>
      <div v-if="showBooking" class="alert-box">
        <p>Are you sure you want to book this appointment?</p>
        <button @click="confirmBooking" id="yes">Yes</button>
        <button @click="closeAlert" id="no">No</button>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent, ref, onMounted } from "vue";
import {jwtDecode} from "jwt-decode";

export default defineComponent({
  setup() {
    const selectedSlot = ref(null);
    const showBooking = ref(false);
    const days = ref([]);
    const URL = `http://localhost:8091/api/calendar/get`;
    const bookingURL = `http://localhost:8091/api/timeSlot/book`;

    const fetchTimeSlots = async () => {
      try {
        const response = await fetch(URL, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            weekNumber: "1",
            ownerId: "675844ff73a9ad601e5a77a9"
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
    const book = ()=>{
      showBooking.value = true;
    };

    const confirmBooking = async () => {
      try {
        await fetch(bookingURL, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            timeSlotId: selectedSlot.value.id,
            userId: jwtDecode(localStorage.getItem("token")).sub,
          }),
        });


        alert(`Appointment has been booked successfully!`);
        showBooking.value = false;
      } catch (error) {
        console.error("Error booking appointment:", error);
        alert("Failed to book the appointment. Please try again later.");
        showBooking.value = false;
      }
    };

    const closeAlert = ()=>{
      showBooking.value=false;
    };

    onMounted(() => {
      fetchTimeSlots();
    });

    return {
      days,
      selectedSlot,
      showBooking,
      formatDate,
      selectSlot,
      book,
      confirmBooking,
      closeAlert,
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

.calendar {
  display: flex;
  justify-content: center;
  gap: 20px;
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


button.selected {
  background: #add8e6;
  border-color: #00f;
  color: #000;
}

.alert-box {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: white;
  border: 1px solid #ccc;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.alert-box p {
  font-size: 1.2rem;
  margin-bottom: 20px;
}

.alert-box button {
  margin: 0 10px;
  padding: 10px 20px;
  font-size: 1rem;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

#bookButton{
  display: inline-block;
  padding: 15px 30px;
  border: 1px solid #ffffff;
  background: #0b6623;
  cursor: pointer;
  font-size: 15px;
  font-weight: bold;
  border-radius: 8px;
  color: #ffffff;
}


#yes{
  background-color: #0b6623;
  color: white;
}
#no{
  background-color: #dc3545;
  color: white;
}

@media (max-width: 768px) {
  .calendar-card {
    margin-bottom: 20px;
  }
}

</style>
