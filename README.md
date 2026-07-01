# SafeRide Backend

SafeRide is a comprehensive backend service built with Spring Boot, designed to manage school transportation, track student rides, and provide dashboards for drivers and parents. 

## 🚀 Features

- **Authentication & Security:** Secure access using JWT (JSON Web Tokens).
- **Firebase Integration:** Uses Firebase Admin SDK for push notifications and real-time database integrations.
- **QR Code Scanning:** Integrates ZXing for generating and scanning student ID QR codes upon boarding or alighting.
- **Role-Based Dashboards & Controllers:** 
  - `DriverDashboardController`: Manage routes and scan student boarding/alighting.
  - `ParentStudentController`: Allow parents to track student rides and view details.
  - `ScanController`: Handle the logic for scanning QR codes.
  - `AuthController`: Handle user registration and login.
- **Email Notifications:** Built-in Spring Mail for automated communication.

## 🛠️ Tech Stack

- **Java 21**
- **Spring Boot 3.2.3**
  - Spring Web
  - Spring WebFlux (for REST calls)
  - Spring Boot Actuator
  - Spring Mail
- **Security:** JJWT (JSON Web Token) for authentication.
- **Firebase Admin SDK (9.3.0)**
- **ZXing (Zebra Crossing) 3.5.3** (QR Code Processing)
- **Build Tool:** Maven

## 📂 Project Structure

- `controller` / `controllers`: REST API endpoints.
- `service` / `services`: Core business logic.
- `model` / `models`: Data entities and domains.
- `dto`: Data Transfer Objects for API requests/responses.
- `security`: JWT and authentication configurations.
- `config`: Application and Firebase configurations.

## ⚙️ Getting Started

### Prerequisites

- [Java 21](https://jdk.java.net/21/) installed
- Maven installed (or use the provided Maven wrapper `mvnw`)
- Firebase Service Account Key (for Firebase integration)

### Installation & Setup

1. **Navigate to the backend directory:**
   ```bash
   cd SafeRide_Backend-Kaveen
   ```

2. **Set up Firebase configuration:**
   - Make sure your Firebase service account credentials JSON file is securely placed and properly referenced in your `application.properties` or `application.yml`.

3. **Build the project:**
   Using Maven wrapper on Windows:
   ```cmd
   mvnw.cmd clean install
   ```
   *(Or just `mvn clean install` if Maven is installed on your system PATH)*

4. **Run the application:**
   ```bash
   mvnw.cmd spring-boot:run
   ```
   The backend server should start on `http://localhost:8080`.

## 🛡️ Security & Authentication

This project utilizes JWT for stateless authentication. To access protected endpoints, include the JWT token in the `Authorization` header of your HTTP requests:

```text
Authorization: Bearer <your_jwt_token_here>
```
