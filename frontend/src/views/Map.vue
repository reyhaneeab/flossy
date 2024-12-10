<template>
  <div class="map-container">
    <GMapMap
      :center="mapCenter"
      :zoom="zoomLevel"
      class="map"
    >
      <GMapMarker
        v-for="dentist in dentists"
        :key="dentist.id"
        :position="{ lat: dentist.latitude, lng: dentist.longitude }"
      />
      <GMapMarker
        v-if="userLocation"
        :position="userLocation"
        icon="http://maps.google.com/mapfiles/ms/icons/blue-dot.png"
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
      mapCenter: { lat: 57.70887, lng: 11.97456 }, // Default to Gothenburg
      zoomLevel: 12,
      userLocation: null,
    };
  },
  mounted() {
    this.fetchDentists();
    this.watchUserLocation();
  },
  methods: {
    async fetchDentists() {
      try {
        const response = await fetch('http://localhost:8084/api/dentists');
        const data = await response.json();
        console.log('Fetched dentists data:', data); // Debugging line
        this.dentists = data.map(dentist => ({
          ...dentist,
          latitude: parseFloat(dentist.latitude),
          longitude: parseFloat(dentist.longitude),
        }));
        console.log('Parsed dentists data:', this.dentists); // Debugging line
      } catch (error) {
        console.error('Error fetching dentist offices:', error);
      }
    },
    watchUserLocation() {
      if (navigator.geolocation) {
        navigator.geolocation.watchPosition(
          position => {
            this.userLocation = {
              lat: position.coords.latitude,
              lng: position.coords.longitude,
            };
            this.mapCenter = { ...this.userLocation };
          },
          error => {
            console.error('Error getting user location:', error);
          },
          { enableHighAccuracy: true, timeout: 5000, maximumAge: 0 }
        );
      }
    },
  },
}
</script>

<style scoped>
.map-container {
  width: 100%;
  height: 100vh;
  position: relative;
}

.map {
  width: 100%;
  height: 100%;
}
</style>