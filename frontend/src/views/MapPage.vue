<template>
  <div class="map-container">
    <Map
      :center="mapCenter"
      :zoom="zoomLevel"
      class="map"
      @load="onMapLoad"
    >
      <!-- Markers will be added programmatically -->
    </Map>
  </div>
</template>

<script setup>
import { ref, shallowRef } from 'vue'
import { Map } from '@fawmi/vue-google-maps'

const URL = `http://localhost:8091/api/dentists/get`

const dentists = ref([])
const markers = ref([])
const mapCenter = ref({ lat: 57.70887, lng: 11.97456 }) // Default to Gothenburg
const zoomLevel = ref(12)
const map = shallowRef(null)

async function fetchDentists() {
  try {
    const response = await fetch(URL, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    })

    if (response.ok) {
      const data = await response.json()
      dentists.value = data.map(dentist => ({
        ...dentist,
        latitude: parseFloat(dentist.latitude),
        longitude: parseFloat(dentist.longitude),
      }))
      addDentistMarkers()
    } else {
      console.error('Error fetching dentist offices:', await response.text())
    }
  } catch (error) {
    console.error('Error fetching dentist offices:', error)
  }
}

function onMapLoad(loadedMap) {
  console.log('Map loaded:', loadedMap)
  map.value = loadedMap
  fetchDentists()
}

function addDentistMarkers() {
  if (!map.value) return

  dentists.value.forEach(dentist => {
    console.log(`Adding marker for dentist: ${dentist.name} at (${dentist.latitude}, ${dentist.longitude})`)

    // eslint-disable-next-line no-undef
    const marker = new google.maps.Marker({
      position: { lat: dentist.latitude, lng: dentist.longitude },
      map: map.value,
      title: dentist.name,
    })

    markers.value.push(marker) // Keep track of added markers

    // Optionally: Add an info window or other UI components
    // eslint-disable-next-line no-undef
    const infoWindow = new google.maps.InfoWindow({
      content: `<h3>${dentist.name}</h3><p>${dentist.address}</p>`,
    })
    marker.addListener('click', () => {
      infoWindow.open(map.value, marker)
    })
  })
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
