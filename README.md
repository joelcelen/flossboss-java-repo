# FlossBoss Java Repository

## Description
The FlossBoss project is a web-based system for dental appointment management that combines a client/server model with a service-oriented approach to efficiently manage dental appointments. Employing a React-based Patient GUI on the front end and an Express.js server on the back end for user data handling through HTTP REST API. The system leverages MQTT as a communication protocol, where the server, instead of direct database operations for bookings, publishes booking requests and subscribes to booking confirmations, enabling real-time updates via SSE. The Service Layer encapsulates the booking logic, dentist authentication, email notifications, and logging, with services subscribing to and publishing on MQTT topics for a decoupled, event-driven workflow that provides real-time notifications, enhancing user experience and system maintainability.

This repository contains the Java codebase for the entire system, including the service layer, dentist client, and admin tool. This setup covers everything from managing appointments and dentist authentication to administrative functions. By segregating these Java-based components from the client/server side, which is developed in JavaScript, the project achieves a clear separation of concerns. This approach not only streamlines development but also significantly simplifies the Continuous Integration and Continuous Delivery (CI/CD) processes, ensuring smoother deployments and easier maintenance of the system.

## Table of contents
1. [System Overview](#system-overview)
2. [Technologies Used](#technologies-used)
3. [Installation guide](#installation-guide)
4. [Authors and Acknowledgments](#authors-and-acknowledgments)
5. [License](#license)

## System overview
The FlossBoss system orchestrates a seamless dental appointment management experience by integrating a distributed architecture that emphasizes real-time data handling and service-oriented design principles. At the core of the system's backend, developed in Java, lies a service layer that interfaces with an MQTT broker, ensuring a decoupled and event-driven communication flow. This backend manages critical operations such as appointment booking, dentist authentication, and system logging.

The diagram below provides a visual representation of the development view for the entire system.
### Development view
<img src="https://i.imgur.com/RaZndv5.png" />

## Technologies used
**Programmning Languages:** Java 17

**Build Tools and Dependency Management:** Maven

**Containerization:** Docker

**Databases:** MongoDB Cloud

**Messaging Protocols:** MQTT (HiveMQ Cloud)

## Installation guide
#### Prerequisites
* Java 17 [Download here](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)



## Authors and Acknowledgments

## License
