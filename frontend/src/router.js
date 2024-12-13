import { createRouter, createWebHistory } from 'vue-router'
import {authService} from "@/authService";

import Calendar from "@/views/CalendarPage.vue";
import Login from "@/views/LoginPage.vue";
import Home from "@/views/HomePage.vue";
import GoogleMap from "@/views/MapPage.vue"; 
import SignUp from "@/views/SignUpPage.vue";

const routes=[
    {path: '/home', name: 'Home', component: Home, meta: { requiresAuth: true }, },
    {path: '/signup', name: 'Signup', component: SignUp, meta: {requiresAuth: false}, },
    {path: '/login', name: 'Login', component: Login, meta: { requiresAuth: false }, },
    {path: '/calendar', name: 'Calendar', component: Calendar, meta: { requiresAuth: true }, },
    {path: '/map', name: 'Map', component : GoogleMap },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/login',
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

//Add a navigation guard
router.beforeEach((to, from, next) => {
    if (to.meta.requiresAuth && !authService.isLoggedIn()) {
        next({ name: 'Login' });
    } else {
        next();
    }
});

export default router