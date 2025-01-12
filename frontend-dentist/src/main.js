import { createApp } from 'vue'
import App from './App.vue'
import router from '@/router'

export { createApp, App, router }

const app = createApp(App)
app.use(router)
app.mount('#app')
