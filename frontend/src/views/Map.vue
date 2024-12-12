<template>
  <div class="map-container">
    <GMapMap
      :center="mapCenter"
      :zoom="zoomLevel"
      class="map"
      @load="onMapLoad"
    >
      <!-- Markers will be added programmatically -->
      <!-- eslint-disable-next-line vue/no-deprecated-slot-attribute -->
      <div slot="control-block-start-inline-start" class="place-picker-container">
        <gmpx-place-picker placeholder="Enter an address" @gmpx-placechange="onPlaceChange"></gmpx-place-picker>
      </div>
    </GMapMap>
  </div>
</template>

<script>
import { GMapMap } from '@fawmi/vue-google-maps'
import { shallowRef, toRaw } from 'vue'

export default {
  name: 'GoogleMap',
  components: {
    GMapMap,
  },
  data() {
    return {
      dentists: [],
      markers: [],
      mapCenter: { lat: 57.70887, lng: 11.97456 }, // Default to Gothenburg
      zoomLevel: 12,
      userLocation: null,
      map: shallowRef(null),
      placePicker: null,
      infowindow: null,
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
        this.dentists = data.map(dentist => ({
          ...dentist,
          latitude: parseFloat(dentist.latitude),
          longitude: parseFloat(dentist.longitude),
        }));
        this.addDentistMarkers();
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
            this.addUserLocationMarker();
          },
          error => {
            console.error('Error getting user location:', error);
          },
          { enableHighAccuracy: true, timeout: 5000, maximumAge: 0 }
        );
      }
    },
    onMapLoad(map) {
      console.log('Map loaded:', map);
      this.map = map;
      this.addDentistMarkers();
      this.addUserLocationMarker();
      this.initPlacePicker();
    },
    addDentistMarkers() {
      if (!this.map) return;

      this.dentists.forEach(dentist => {
        console.log(`Adding marker for dentist: ${dentist.name} at (${dentist.latitude}, ${dentist.longitude})`);

        // eslint-disable-next-line no-undef
        const marker = new google.maps.marker.AdvancedMarkerElement({
          position: { lat: dentist.latitude, lng: dentist.longitude },
          map: this.map, // Ensure 'this.map' is passed directly
          title: dentist.name,
        });

        this.markers.push(marker); // Keep track of added markers
      });
    },
    addUserLocationMarker() {
      if (!this.map || !this.userLocation) return;
      console.log(`Adding user location marker at (${this.userLocation.lat}, ${this.userLocation.lng})`);
      // eslint-disable-next-line no-undef
      new google.maps.marker.AdvancedMarkerElement({
        map: toRaw(this.map),
        position: this.userLocation,
        icon: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png',
      });
    },
    initPlacePicker() {
      this.placePicker = document.querySelector('gmpx-place-picker');
      // eslint-disable-next-line no-undef
      this.infowindow = new google.maps.InfoWindow();
    },
    onPlaceChange() {
      const place = this.placePicker.value;

      if (!place.location) {
        window.alert("No details available for input: '" + place.name + "'");
        this.infowindow.close();
        return;
      }

      if (place.viewport) {
        this.map.innerMap.fitBounds(place.viewport);
      } else {
        this.map.center = place.location;
        this.map.zoom = 17;
      }

      // eslint-disable-next-line no-undef
      const marker = new google.maps.marker.AdvancedMarkerElement({
        position: place.location,
        map: this.map,
      });

      this.infowindow.setContent(
        `<strong>${place.displayName}</strong><br>
         <span>${place.formattedAddress}</span>`
      );
      this.infowindow.open(this.map.innerMap, marker);
    }
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

.place-picker-container {
  padding: 20px;
}
</style>