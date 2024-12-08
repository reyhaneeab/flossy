import { createRouter, createWebHistory } from 'vue-router'

import Calendar from "@/views/CalendarPage.vue";
import Signup from "@/views/SignUpPage.vue";
import Login from "@/views/LoginPage.vue";
import Home from "@/views/HomePage.vue";
import Appointment from "@/views/AppointmentPage.vue";
//import Success from "@/views/SuccessPage.vue";

const routes=[
    {path: '/', name: 'Home', component: Home },
    {path: '/signup', name: 'Signup', component: Signup },
    {path: '/login', name: 'Login', component: Login },
    {path: '/calendar', name: 'Calendar', component: Calendar },
    {path: '/appointment', name:'Appointment', component: Appointment}
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router