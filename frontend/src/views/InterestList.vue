<template>
  <div class="interest-list">
    <h1>Interest List</h1>
    <ul>
      <li v-for="item in interestItems" :key="item.id" @click="goToCalendar()">
        {{ item.name }}
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  name: "InterestList",
  data() {
    return {
      interestItems: [
        { id: 1, name: "Health House" },
        { id: 2, name: "GU Dental" },
        { id: 3, name: "Happy teeth" },
        { id: 4, name: "Dental Clinic" },
        { id: 5, name: "Dental Care" },
      ],
    };
  },
  methods: {
    goToCalendar() {
      this.$router.push({ name: 'Calendar', params: { ownerId: 123, weekNumber: 51 } }); 
    }
  }
};
</script>

<style scoped>
.interest-list {
  padding: 20px;
  background-color: #EDE8F5; 
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center; 
  width: 80%;
  max-width: 600px;
  margin: 0 auto;
}

.interest-list h1 {
  color: #3D52A0; 
}

.interest-list ul {
  list-style-type: none;
  padding: 0;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center; 
}

.interest-list li {
  width: 80%;
  max-width: 400px;
  padding: 10px;
  background-color: #fff;
  border: 1px solid #8697C4; 
  margin-bottom: 10px;
  border-radius: 8px;
  color: #3D52A0; 
  cursor: pointer;
  transition: background-color 0.3s, transform 0.2s;
  text-align: center;
}

.interest-list li:hover {
  background-color: #f0f0f0;
  transform: scale(1.02);
}
</style>
