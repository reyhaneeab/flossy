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
                      :class="{ 'btn-primary text-black': slot.selected, 'disabled': slot.booked }"
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
import { useRoute } from 'vue-router';

export default defineComponent({
  setup() {
    const selectedSlot = ref(null);
    const showBooking = ref(false);
    const days = ref([]);
    const bookingURL = `http://localhost:8091/api/timeSlot/book`;
    const route = useRoute();
    const dentistId = route.params.dentistId; // Get dentistId from route params
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
                  ownerId: dentistId // Ensure dentistId is used here
                })
              })

              const data = await response.json();

              // Group the time slots by day and create a proper structure for `days`
              const dayMap = {};

        data.timeSlots.forEach(slot => {
          const date =`${slot.date[0]}-${String(slot.date[1]).padStart(2, '0')}-${String(slot.date[2]).padStart(2, '0')}`;
          if (!dayMap[date]) {
            dayMap[date] = {
              date: date,
              slots: []
            };
          }
          const startTime = new Date(slot.startTime[0], slot.startTime[1] - 1, slot.startTime[2], slot.startTime[3], slot.startTime[4]);
          const endTime = new Date(slot.endTime[0], slot.endTime[1] - 1, slot.endTime[2], slot.endTime[3], slot.endTime[4]);

          // Push the slot details into the correct day
          dayMap[date].slots.push({
            id: slot.id,
            time: `${startTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} - ${endTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`,
            booked: slot.booked,
            selected: false,
            ownerName: slot.owner ? slot.owner.name : 'Unknown'
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
              const response=await fetch(bookingURL, {
                method: "POST",
                headers: {
                  "Content-Type": "application/json",
                },
                body: JSON.stringify({
                  timeSlotId: selectedSlot.value.id,
                  userId: jwtDecode(localStorage.getItem("token")).sub,
                }),
              });
              if (!response.ok) {
                const errorData = await response.json();
                alert(`Appointment is already booked: ${errorData.error}`);
                return;
              }


              alert(`Appointment has been booked successfully!`);
              showBooking.value = false;
            } catch (error) {
              console.error("Error booking appointment:", error);
              alert("Appointment is already booked. Please try other time slot.");
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
.text-center mb-4{
  color: #EDE8F5;
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
  background-color: #3D52A0;
  border-bottom: 2px solid #3D52A0;
}

.card-title {
  color: #EDE8F5;
  font-size: 1.2rem;
  margin-bottom: 0;
}
.card-body{
  background-color: #3D52A0;
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
  background-color: #3D52A0;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
h1{
  color:#3D52A0;
  text-shadow:0 2px 4px rgba(0, 0, 0, 0.6);
}
 h2 {
  color: #EDE8F5;
  text-shadow:0 2px 4px rgba(0, 0, 0, 0.6);
}

button {
  padding: 30px 50px;
  border: 1px solid #7091E6;
  border-radius: 10px;
  background: #ADBBDA;
  cursor: pointer;
  transition: 0.3s;
  font-size: 15px;
  font-weight: bold;
  box-shadow: 0 4px 6px #7091E6(0, 0, 0, 0.6);
  color:#3D52A0;
  text-shadow: 0 4px 6px #rgba(0, 0, 0, 0.6);
}

button.selected {
  background: #add8e6;
  border-color: #EDE8F5;
  color: #7091E6;
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
  padding: 20px 40px;
  border: 1px solid #000000;
  background: #7091E6;
  cursor: pointer;
  font-size: 15px;
  font-weight: bold;
  border-radius: 10px;
  color: #EDE8F5;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.6);
  text-shadow: 0 4px 6px rgba(0, 0, 0, 0.4);
}

#yes{
  background-color: #4169e1;
  color: white;
}
#no{
  background-color: #d10000;
  color: white;
}

@media (max-width: 768px) {
  .calendar-card {
    margin-bottom: 20px;
  }
}
</style>
