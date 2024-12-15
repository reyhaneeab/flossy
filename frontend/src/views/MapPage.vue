<template>
  <div class="map-container" ref="mapContainer">
  
  </div>
</template>

<script setup>

/* global google */
import { onMounted, ref } from 'vue'
import api from "@/api";

const mapContainer = ref(null)
const mapCenter = { lat: 57.70887, lng: 11.97456 } // Default to Gothenburg
const zoomLevel = 12
const map = ref(null)
const dentists = ref([])
const markers = ref([])

onMounted(() => {
  window.initMap = initMap // Ensure initMap is defined in the global scope
  if (window.google && window.google.maps) {
    initMap()
  }
})

function initMap() {
  map.value = new google.maps.Map(mapContainer.value, {
    center: mapCenter,
    zoom: zoomLevel,
  })
  fetchDentists()
}

async function fetchDentists() {
  try {
    const response = await api.post('dentist/get')
    dentists.value = response.data
    convertAddressesToCoordinates()
  } catch (error) {
    console.error('Error fetching dentist offices:', error)
  }
}

async function convertAddressesToCoordinates() {
  const geocoder = new google.maps.Geocoder()

  for (const dentist of dentists.value) {
    const { address, name } = dentist
    try {
      const result = await geocodeAddress(geocoder, address)
      const latitude = result.lat()
      const longitude = result.lng()
      addDentistMarker({ address, name, latitude, longitude })
    } catch (error) {
      console.error(`Error geocoding address for ${name}:`, error)
    }
  }
}

function geocodeAddress(geocoder, address) {
  return new Promise((resolve, reject) => {
    geocoder.geocode({ address }, (results, status) => {
      if (status === 'OK') {
        resolve(results[0].geometry.location)
      } else {
        reject(`Geocode was not successful for the following reason: ${status}`)
      }
    })
  })
}

function addDentistMarker(dentist) {
  if (!map.value) return

  const marker = new google.maps.Marker({
    position: { lat: dentist.latitude, lng: dentist.longitude },
    map: map.value,
    title: dentist.officeName,
  })

  markers.value.push(marker)

  const infoWindow = new google.maps.InfoWindow({
    content: `<div style="color: black;">
                <h3>${dentist.officeName}</h3>
                <p>${dentist.address}</p>
                <p>${dentist.phone}</p>
              </div>`,
  })
  marker.addListener('click', () => {
    infoWindow.open(map.value, marker)
  })
}
</script>

<style scoped>
.map-container {
  width: 100%;
  height: 100vh;
  position: relative;
}
</style>
