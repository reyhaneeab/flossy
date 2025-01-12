import { createRouter, createWebHistory } from 'vue-router'

import WelcomePage from "@/views/WelcomePage.vue";
import SchedulePage from "@/views/SchedulePage.vue";

const routes=[
    {path: '/', name: 'WelcomePage', component: WelcomePage},
    {path: '/schedule', name: 'SchedulePage', component: SchedulePage},
    ]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router