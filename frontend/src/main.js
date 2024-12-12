import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import VueGoogleMaps from '@fawmi/vue-google-maps'

const app = createApp(App)
app.use(router)
app.use(VueGoogleMaps, {
  load: {
    key: process.env.VUE_APP_GOOGLE_MAPS_API_KEY,
    libraries: 'places,marker', // This is required if you use the Autocomplete plugin
    async: true, // Ensure async loading
    v: 'weekly' // Specify the version to use a supported version
  },
})
app.mount('#app')