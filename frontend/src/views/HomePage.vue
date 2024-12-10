<template>
  <div>
    <h1>Welcome to the Home Page!</h1>
    <button @click="logout">Logout</button>
  </div>
</template>

<script>
import { authService } from '@/authService';
import { useRouter } from 'vue-router';

export default {
  setup() {
    const router = useRouter();

    const logout = () => {
      authService.logout();
      router.push('/login'); // Redirect to login
    };

    return { logout };
  },
};
</script>

<style scoped>
h1 {
  color: #333;
  text-align: center;
}
p {
  text-align: center;
  color: #666;
}
</style>