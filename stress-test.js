import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
    stages: [
        { duration: '10s', target: 1000 }, // Ramp-up to 1000 users in 10 seconds
        { duration: '5s', target: 1000 }, // Stay at 1000 users for 5 seconds
        { duration: '3s', target: 700 },    // Ramp-down to 700 users in 3 seconds
    ],
};

export default function () {
    const payload = JSON.stringify({
        weekNumber: "1",
        ownerId: "675844ff73a9ad601e5a77a9", // Replace with your own dentist id
    });

    const params = {
        headers: {
            "Content-Type": "application/json",//accepted file type
        },
    };
    let response = http.post('http://localhost:8091/api/calendar/get',payload,params);
    check(response, {
        'is status 200': (r) => r.status === 200,//depending on response, display appropriate message
        'is status 500': (r)=>r.status===500,
        'response is not empty': (r) => r.body && r.body.length > 0,
        'retrieve the calendar': (r) => !r.error,
    });
    sleep(1);


// Payload for the request
   /* const payload = JSON.stringify({
        weekNumber: "1",
        ownerId: "675844ff73a9ad601e5a77a9", // Replace with your own dentist id
    });

    // Request headers
    const params = {
        headers: {
            "Content-Type": "application/json",
        },
    };

    // Perform POST request
    let response = http.post('http://localhost:8091/api/calendar/get', payload, params);



    // Add checks to verify the response
    check(response, {
        'is status 200': (r) => r.status === 200,
        'is status 500': (r)=>r.status===500,
        'response is not empty': (r) => r.body && r.body.length > 0,
        'no timeouts': (r) => !r.error,
    });

    // Sleep to simulate user think time
    sleep(1);*/
}