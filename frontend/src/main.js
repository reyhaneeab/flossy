import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import VueGoogleMaps from '@fawmi/vue-google-maps'

const app = createApp(App)
app.use(router)
app.use(VueGoogleMaps, {
  load: {
    key: 'AIzaSyD0d8O6v_7_CBdIobTLvfTGRjGLcd_DSbs',
    libraries: 'places', // This is required if you use the Autocomplete plugin
  },
})
app.mount('#app')