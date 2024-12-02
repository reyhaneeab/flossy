# Flossy

> _The next best way to care about your teeth!_

![logo](documentation/images/logo.png)

---

## **Table of Contents**
1. [About the Project](#about-the-project)  
2. [Getting Started](#getting-started)  
   - [Prerequisites](#prerequisites)  
   - [Installation](#installation)  
3. [Usage](#usage)  
4. [Contributing](#contributing)  
5. [License](#license)
6. [Documentation](#documentation)  
6. [Team members and Acknowledgments](#team-members-and-acknowledgments)

## **About the Project**

**Flossy** is a **distributed system** designed for managing dental appointment bookings. The project follows a **Service-Oriented Architecture (SOA)**, utilizing a **client-server** model with separate services for dentists, users, appointments, calendars, and notifications.  

The application features an **event-driven design** using the **publish/subscribe** messaging paradigm powered by the **MQTT Mosquitto broker** for efficient and reliable communication between components.  

The primary focus is on scalability, modularity, and simplicity, making it easier to maintain, extend, and deploy the application. **Docker Compose** is used to streamline the setup of key components like MongoDB and MQTT.

Frontend development is handled using **Vue.js**, while backend services are implemented in **Java (Spring Boot)**.

## **Getting Started**

### **Prerequisites**
Ensure you have the following installed on your system:  
- **[Java Development Kit (JDK) version 21 LTS](https://openjdk.org/)**  
- **[Maven 3.8+](https://maven.apache.org/download.cgi)**  
- **[Docker and Docker Compose](https://www.docker.com/)**  
- **[Node.js 16+](https://nodejs.org/)**  
- **[MongoDB](https://www.mongodb.com)**

---

### **Installation**
1. Clone the repository:  
   ```bash
   git clone <your-SSH-or-HTTPS-key>
   cd flossy

2. Download and compile all the dependencies:
    ```bash
   mvn clean install
   mvn verify

3. Run the dockerized utility services:
    ```bash
    docker-compose up -d

4. Install dependecies and run the client (in production mode):
    ```bash
    cd frontend
    npm install
    npm run build
    npm run serve

5. Finally to start any service individually:
    ```bash
    cd <service-name>
    mvn spring-boot:run

    Note: repeat the process for each service you want to run.


## **Used Technologies**
The following list contains the technologies used in this project:

### **Backend**
- **Java 21**: Programming language for developing the backend services.
- **Spring Boot**: Framework for creating APIs and services.
- **Maven**: Dependency management and build tool.

### **Frontend**
- **Vue.js**: JavaScript framework for building the user interface.
- **Node.js**: For managing dependencies and running frontend tools.

### **Messaging**
- **MQTT Mosquitto Broker**: Publish/subscribe messaging system for real-time communication between services.
- **Paho MQTT Client Library**: For a conviniest and easy integration of MQTT functionality into backend.

### **Database**
- **MongoDB**: NoSQL database for storing persistent data like users, dentists, appointments, and calendars.

### **Deployment and Orchestration**
- **Docker**: Containerization of services for consistent and isolated environments.
- **Docker Compose**: For managing multi-container environments like MongoDB and Mosquitto.

### **Development Tools**
- **IntelliJ IDEA**: Integrated Development Environment (IDE) for backend development.
- **VS Code**: Editor for frontend development.
- **Git**: Version control system.
- **GitLab**: Code hosting platforms for collaboration and repository management.

### **Others**
- **JSON Web Tokens (JWT)**: For secure authentication and session management.
- **REST APIs**: For communication between API Gateway and the client.


## **Contributing**
Contributions to the project are very welcome since it is open-source. If you would like to contribute, please follow these steps:

1. Fork the project:
    ```bash
    git fork https://github.com/<your-username>/flossy.git

2. Create a feature branch:
     ```bash
     git checkout -b feature/AmazingFeature

3. Work and commit your changes, following the [commit template](https://git.chalmers.se/courses/dit355/2024/student_teams/dit356_2024_11/flossy/-/wikis/Commit-Message-Template)
    ```bash
    git commit -m "#_: add some AmazingFeature"

4. Push your local changes:
    ```bash
    git push origin feature/AmazingFeature

5. Open a Pull/Merge Request so it can be reviewed!


## **License**
The project is licensed under the MIT License. Refer to the [LICENSE](https://git.chalmers.se/courses/dit355/2024/student_teams/dit356_2024_11/flossy/-/blob/development/LICENSE?ref_type=heads) file for more information.


## **Documentation**
For the full documentation of the project, see our [Wiki](https://git.chalmers.se/courses/dit355/2024/student_teams/dit356_2024_11/flossy/-/wikis/home).
You can navigate between the different pages, using the panel on the right side or the TOC.

## **Team members and Acknowledgments**
- Reihaneh Abbasi (abbasir@student.chalmers.se)
- René Ignacio González Pérez (renei@student.chalmers.se)
- Nika Balouchzadeh (nikab@student.chalmers.se)
- Laura Swiecicka (laurasw@student.chalmers.se)
- Georgi Penchev (penchev@student.chalmers.se)





  
