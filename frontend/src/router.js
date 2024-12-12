import { createRouter, createWebHistory } from 'vue-router'

import Calendar from "@/views/CalendarPage.vue";
import Signup from "@/views/SignUpPage.vue";
import Login from "@/views/LoginPage.vue";
import Home from "@/views/HomePage.vue";
import Appointments from "@/views/AppointmentsPage.vue";
//import Success from "@/views/SuccessPage.vue";

const routes=[
    {path: '/home', name: 'Home', component: Home, meta: { requiresAuth: true }, },
    {path: '/login', name: 'Login', component: Login, meta: { requiresAuth: false }, },
    {path: '/signup', name: 'Signup', component: Signup, meta: {requiresAuth: false} },
    {path: '/calendar', name: 'Calendar', component: Calendar, meta: { requiresAuth: true }, },
    {path: '/appointments', name: 'Appointments', component: Appointments, meta: { requiresAuth: true }, },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/login',
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes
})



// Add a navigation guard
/*router.beforeEach((to, from, next) => {
    if (to.meta.requiresAuth && !authService.isLoggedIn()) {
        next({ name: 'Login' });
    } else {
        next();
    }
});
*/


export default router