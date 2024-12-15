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
const currentLocationMarker = ref(null)
let mapInitialized = false // Track if the map is initialized

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
  mapInitialized = true // Set to true so the map centers on the user's location on the first update

  // Check if the user's location is stored in local storage
  const storedLocation = localStorage.getItem('userLocation')
  if (storedLocation) {
    const pos = JSON.parse(storedLocation)
    setCurrentLocationMarker(pos)
    map.value.setCenter(pos)
    mapInitialized = false
  } else {
    getCurrentLocation() // Get the user's current location
  }
  fetchDentists() // Fetch the dentist offices
}

// Get the user's current location and update the map
function getCurrentLocation() {
  if (navigator.geolocation) {
    navigator.geolocation.watchPosition(
      (position) => {
        const pos = {
          lat: position.coords.latitude,
          lng: position.coords.longitude,
        }
        localStorage.setItem('userLocation', JSON.stringify(pos))
        setCurrentLocationMarker(pos)
        if (mapInitialized) {
          map.value.setCenter(pos)
          mapInitialized = false // Only center the map on the first location update
        }
      },
      (error) => {
        console.error('Error getting current location:', error)
      },
      { timeout: 1000 } // Add a timeout of 1 seconds
    )
  } else {
    console.error('Geolocation is not supported by this browser.')
  }
}

// Set the current location marker on the map or update its position
function setCurrentLocationMarker(pos) {
  if (!currentLocationMarker.value) {
    currentLocationMarker.value = new google.maps.Marker({
      position: pos,
      map: map.value,
      title: 'Your Current Location',
      icon: {
        url: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png',
        scaledSize: new google.maps.Size(50, 50) // Increase the size of the icon
      },
    })
  } else {
    currentLocationMarker.value.setPosition(pos)
  }
}

// Fetch the dentist offices from the API and add them to the map
async function fetchDentists() {
  try {
    const response = await api.post('dentist/get')
    dentists.value = response.data
    convertAddressesToCoordinates()
  } catch (error) {
    console.error('Error fetching dentist offices:', error)
  }
}

// Convert addresses to coordinates using the Geocoder API
async function convertAddressesToCoordinates() {
  const geocoder = new google.maps.Geocoder()

  for (const dentist of dentists.value) {
    // Check if address is valid before calling geocode
    if (dentist.address && typeof dentist.address === 'string') {
      try {
        console.log('Processing address:', dentist.address);
        const result = await geocodeAddress(geocoder, dentist.address);
        dentist.latitude = result.lat();
        dentist.longitude = result.lng();
        addDentistMarker(dentist);
      } catch (error) {
        console.error(`Error geocoding address for ${dentist.officeName}:`, error);
      }
    } else {
      console.error('Invalid address:', dentist);
    }
  }
}

// Geocode an address to get its coordinates using the Geocoder API
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

// Add a marker for a dentist office to the map with an info window
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
                <a href="/calendar/${dentist.id}" class="btn btn-primary">Appointments</a> <!-- Ensure dentist.id is used here -->
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
