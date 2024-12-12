<template>
  <div class="map-container">
    <Map
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
    </Map>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Map } from '@fawmi/vue-google-maps'
import { shallowRef } from 'vue'

const URL = `http://localhost:8091/api/dentists`

const dentists = ref([])
const markers = ref([])
const mapCenter = ref({ lat: 57.70887, lng: 11.97456 }) // Default to Gothenburg
const zoomLevel = ref(12)
const map = shallowRef(null)
const placePicker = ref(null)
const infowindow = ref(null)

async function fetchDentists() {
  try {
    const response = await fetch(URL)
    const data = await response.json()
    dentists.value = data.map(dentist => ({
      ...dentist,
      latitude: parseFloat(dentist.latitude),
      longitude: parseFloat(dentist.longitude),
    }))
    addDentistMarkers()
  } catch (error) {
    console.error('Error fetching dentist offices:', error)
  }
}

function onMapLoad(loadedMap) {
  console.log('Map loaded:', loadedMap)
  map.value = loadedMap
  addDentistMarkers()
  initPlacePicker()
}

function addDentistMarkers() {
  if (!map.value) return

  dentists.value.forEach(dentist => {
    console.log(`Adding marker for dentist: ${dentist.name} at (${dentist.latitude}, ${dentist.longitude})`)

    // eslint-disable-next-line no-undef
    const marker = new google.maps.marker.AdvancedMarkerElement({
      position: { lat: dentist.latitude, lng: dentist.longitude },
      map: map.value, // Ensure 'map.value' is passed directly
      title: dentist.name,
    })

    markers.value.push(marker) // Keep track of added markers
  })
}

function initPlacePicker() {
  placePicker.value = document.querySelector('gmpx-place-picker')
  // eslint-disable-next-line no-undef
  infowindow.value = new google.maps.InfoWindow()
}

function onPlaceChange() {
  const place = placePicker.value.value

  if (!place.location) {
    window.alert("No details available for input: '" + place.name + "'")
    infowindow.value.close()
    return
  }

  if (place.viewport) {
    map.value.innerMap.fitBounds(place.viewport)
  } else {
    map.value.center = place.location
    map.value.zoom = 17
  }

  // eslint-disable-next-line no-undef
  const marker = new google.maps.marker.AdvancedMarkerElement({
    position: place.location,
    map: map.value,
  })

  infowindow.value.setContent(
    `<strong>${place.displayName}</strong><br>
     <span>${place.formattedAddress}</span>`
  )
  infowindow.value.open(map.value.innerMap, marker)
}

fetchDentists()
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