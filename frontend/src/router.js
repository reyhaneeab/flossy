import { createRouter, createWebHistory } from 'vue-router'
import {authService} from "@/authService";

import About from "@/views/AboutPage.vue";
import Calendar from "@/views/CalendarPage.vue";
import Login from "@/views/LoginPage.vue";
import Home from "@/views/HomePage.vue";
import GoogleMap from "@/views/MapPage.vue"; 
import SignUp from "@/views/SignUpPage.vue";
import Appointments from "@/views/AppointmentsPage.vue";

const routes=[
    {path: '/home', name: 'Home', component: Home, meta: { requiresAuth: true }, },
    {path: '/about', name: 'AboutUs', component: About, meta: {requiresAuth: false}, },
    {path: '/signup', name: 'Signup', component: SignUp, meta: {requiresAuth: false, reqLog: true}, },
    {path: '/login', name: 'Login', component: Login, meta: { requiresAuth: false, reqLog: true }, },
    {path: '/calendar/:pathMatch(.*)*', name: 'Calendar', component: Calendar },
    {path: '/map', name: 'Map', component : GoogleMap },
    {path: '/appointments', name: 'Appointments', component:Appointments, meta: {requiresAuth:true},},
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

//Add a navigation guard
router.beforeEach((to, from, next) => {
    if (to.meta.requiresAuth && !authService.isLoggedIn()) {
        alert("You need to be logged in to your account to view this page!")
        next({ name: 'Login' });
    }
    else if(to.meta.reqLog && authService.isLoggedIn()){
        alert("You are already logged in to your account!")
        next({ name: 'Home'})
    }
    else {
        next();
    }
});

export default router