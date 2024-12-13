import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    // Key configurations for Stress in this section
    stages: [
        { duration: '10s', target: 1000 }, // traffic ramp-up from 1 to a higher 200 users over 10 minutes.
        { duration: '1s', target: 1000 }, // stay at higher 200 users for 30 minutes
        { duration: '10s', target: 1000 }, // ramp-down to 0 users
    ],
};

export default function () {
    const uniqueEmail = `user_${__ITER}@test.com`; // Unique email using the iteration number
    const payload = JSON.stringify({
        officeName: "hello",
        address: "12",
        email: uniqueEmail,
        password: "1234",
        phone: "1234"
    });
    const params = {
        headers: {
            "Content-Type": "application/json",
        },
    };

    http.post('http://localhost:8091/api/dentist/registration', payload, params);
    sleep(1);
}