<template>
  <div id="map" style="height: 100vh; width: 100%;"></div>
</template>

<script>
import L from 'leaflet'; // Import Leaflet
import 'leaflet-control-geocoder/dist/Control.Geocoder.css'; // Import Geocoder CSS
import 'leaflet-control-geocoder'; // Import Geocoder JS

export default {
  name: 'OSMMap',
  data() {
    return {
      map: null,
      dentists: [],
    };
  },
  mounted() {
    // Initialize the map
    this.map = L.map('map').setView([60.128161, 18.643501], 5); // Center of Sweden

    // Set OpenStreetMap tile layer
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(this.map);

    // Add the search control to the map
    L.Control.geocoder({
      defaultMarkGeocode: false
    })
    .on('markgeocode', (e) => {
      const bbox = e.geocode.bbox;
      const poly = L.polygon([
        bbox.getSouthEast(),
        bbox.getNorthEast(),
        bbox.getNorthWest(),
        bbox.getSouthWest()
      ]).addTo(this.map);
      this.map.fitBounds(poly.getBounds());
    })
    .addTo(this.map);

    // Fetch dentist offices data from backend API
    this.fetchDentists();
  },
  methods: {
    async fetchDentists() {
      try {
        const response = await fetch('http://localhost:8080/api/dentists');
        this.dentists = await response.json();
        this.addMarkers();
      } catch (error) {
        console.error('Error fetching dentist offices:', error);
      }
    },
    addMarkers() {
      this.dentists.forEach(dentist => {
        const marker = L.marker([dentist.latitude, dentist.longitude]).addTo(this.map);
        marker.bindPopup(`<h3>${dentist.officeName}</h3><p>${dentist.address}</p>`);
      });
    },
  },
};
</script>

<style scoped>
#map {
  height: 100vh;
  width: 100%;
}
html, body {
  height: 100%;
  margin: 0;
  padding: 0;
}
</style>