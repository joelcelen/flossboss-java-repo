# FlossBoss Java Repository

## Description
The FlossBoss project is a web-based system for dental appointment management that combines a client/server model with a service-oriented approach to efficiently manage dental appointments. Employing a React-based Patient GUI on the front end and an Express.js server on the back end for user data handling through HTTP REST API. The system leverages MQTT as a communication protocol, where the server, instead of direct database operations for bookings, publishes booking requests and subscribes to booking confirmations, enabling real-time updates via SSE. The Service Layer encapsulates the booking logic, dentist authentication, email notifications, and logging, with services subscribing to and publishing on MQTT topics for a decoupled, event-driven workflow that provides real-time notifications, enhancing user experience and system maintainability.

This repository contains the Java codebase for the entire system, including the service layer, dentist client, and admin tool. This setup covers everything from managing appointments and dentist authentication to administrative functions. By segregating these Java-based components from the client/server side, which is developed in JavaScript, the project achieves a clear separation of concerns. This approach not only streamlines development but also significantly simplifies the Continuous Integration and Continuous Delivery (CI/CD) processes, ensuring smoother deployments and easier maintenance of the system.

## Table of contents
1. [System Overview](#system-overview)
2. [Technologies and Tools Used](#technologies-and-tools-used-in-this-repository)
3. [Installation guide](#installation-guide)
4. [Authors and Acknowledgments](#authors-and-acknowledgments)
5. [License](#license)

## System overview
The FlossBoss system orchestrates a seamless dental appointment management experience by integrating a distributed architecture that emphasizes real-time data handling and service-oriented design principles. At the core of the system's backend, developed in Java, lies a service layer that interfaces with an MQTT broker, ensuring a decoupled and event-driven communication flow. This backend manages critical operations such as appointment booking, dentist authentication, and system logging.

The diagram below provides a visual representation of the development view for the entire system.
### Development view
<img src="https://i.imgur.com/RaZndv5.png" />

## Technologies and Tools used in this repository
**Programmning Languages:** Java 17

**Build Tools and Dependency Management:** Maven

**Containerization:** Docker

**Databases:** MongoDB Cloud

**Messaging Protocols:** MQTT (HiveMQ Cloud)

## Run with Docker

All the services and the client/server are uploaded to the projects [Dockerhub page](https://hub.docker.com/u/flossboss).

To run docker images on your system you need to first install the [docker engine](https://www.docker.com/) on your local system. We recommend installing the desktop version to easily keep track of your current containers and CPU usage.

Due to a current issue with the docker engine, Arm64 and Amd64 systems are not compatible with images built on the opposing architecture. To solve this problem we have images for both Arm64 and Amd64, choose the one that matches you system architecture.

#### Run on Arm64 architecture
1. Navigate to the repository root folder.
2. To start services run "docker compose -f compose.arm.yaml up -d"
3. To exit services run "docker compose -f compose.arm.yaml down"

#### Run on Amd64 architecture
1. Navigate to the repository root folder.
2. To start services run "docker compose -f compose.amd.yaml up -d"
3. To exit services run "docker compose -f compose.amd.yaml down"

## Installation guide
#### Prerequisites
* Java 17 [Link to download](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
* Maven [Link to download](https://maven.apache.org/download.cgi)
* Docker Desktop [Link to download](https://www.docker.com/products/docker-desktop/)
* MongoDB Cloud database [Link to MongoDB](https://account.mongodb.com/account/login?signedOut=true)
* HiveMQ Cloud MQTT Broker [Link to HiveMQ](https://auth.hivemq.cloud/login?state=hKFo2SByblBzUXBWYVZhdkNSYlhPQ3NHUi1BMFNHcFRpVnFZRqFupWxvZ2luo3RpZNkgd0U1VUNSTlZFM1ZFNHZ0SW9jWWhqS2lodHJNSmYta0qjY2lk2SBJYWpvNGUzMmp4d1VzOEFkRnhneFFuMlZQM1l3SVpUSw&client=Iajo4e32jxwUs8AdFxgxQn2VP3YwIZTK&protocol=oauth2&audience=hivemq-cloud-api&redirect_uri=https%3A%2F%2Fconsole.hivemq.cloud&scope=openid%20profile%20email&response_type=code&response_mode=query&nonce=UjJhUnZOUlJnd3RmbjZmNFBGWX5uc2w3bHZERW5tRmVHMHl6MDFjXzVMbQ%3D%3D&code_challenge=cOpID4Iew7D-HcwtkQjs-7GYcfrwzD7JV9QTPQNOJgU&code_challenge_method=S256&auth0Client=eyJuYW1lIjoiYXV0aDAtc3BhLWpzIiwidmVyc2lvbiI6IjEuMjIuNiJ9)
#### Step 1: Edit configuration files
> * Open each service in an IDE that supports Java (we recommend IntelliJ or Visual Studio Code). 
>
> * Add a text file named **"atlasconfig.txt"** to flossboss-java-repo\\**{"service"}**\src\main\resources. Add the MongoDB database URI to the text file.
> * Add a text file named **"hiveconfig.txt"** to flossboss-java-repo\\**{"service"}**\src\main\resources. In the text file you will need to add **4 lines:**
>> **Line 1:** Preffered MQTT **client name**  
>> **Line 2:** **URL** for your HiveMQ cluster  
>> **Line 3:** HiveMQ **email**  
>> **Line 4:** HiveMQ **password**  
>

#### Step 2: Build with Maven
1. Navigate to the specified services root folder.
2. Run "mvn clean install".
3. Run "mvn clean package".
4. Navigate to the subfolder "target".
5. Run "java -jar your-jarfile-name.jar"


## Authors and Acknowledgments
- Isaac Lindegren Ternbom  
- Joel Celén  
- Karl Eriksson  
- Ahmand Haj Ahmad  
- Malte Bengtsson  
- Rizwan Rafiq


## License
The FlossBoss project is licensed under [MIT](https://git.chalmers.se/courses/dit355/2023/student-teams/dit356-2023-16/flossboss-java-repo/-/blob/main/LICENSE)
