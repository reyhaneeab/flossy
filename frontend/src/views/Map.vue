<template>
  <div>
    <GMapMap
      :center="{ lat: 60.128161, lng: 18.643501 }"
      :zoom="5"
      style="width: 100%; height: 100vh"
    >
      <GMapMarker
        v-for="dentist in dentists"
        :key="dentist.id"
        :position="{ lat: dentist.latitude, lng: dentist.longitude }"
      />
    </GMapMap>
  </div>
</template>

<script>
import { GMapMap, GMapMarker } from '@fawmi/vue-google-maps'

export default {
  name: 'GoogleMap',
  components: {
    GMapMap,
    GMapMarker,
  },
  data() {
    return {
      dentists: [],
      markers: [],
    };
  },
  mounted() {
    this.fetchDentists();
  },
  methods: {
    async fetchDentists() {
      try {
        const response = await fetch('http://localhost:8080/api/dentists');
        this.dentists = await response.json();
      } catch (error) {
        console.error('Error fetching dentist offices:', error);
      }
    },
  },
}
</script>

<style scoped>
/* Remove styles related to the search bar */
</style>