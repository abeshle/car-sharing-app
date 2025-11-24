# 🚗 Car Sharing REST API

This project is a backend-only **Car Sharing application** built with Spring Boot.  
It provides a secure REST API for managing cars, users, rentals, payments, and Telegram notifications.

The application supports two roles:

| Role       | Description |
|------------|-------------|
| **CUSTOMER** | Can browse cars, create rentals, return cars, make payments |
| **MANAGER**  | Can create/update/delete cars, manage users, view all rentals & payments |

---

## 🚀 Features

### Authentication & Authorization
- JWT-based login & registration
- Role-based access control (CUSTOMER, MANAGER)

### Car Management
- List all cars 
- View car details
- Create / Update / Delete cars (MANAGER only)
- Inventory tracking

### Rentals
- Create rental (decreases inventory by 1)
- Return rental (increases inventory by 1)
- Filter rentals by user or active status
- Customers see only their rentals
- Managers see all rentals

### Payments (Stripe)
- Create Stripe Checkout Session
- Handle payments and fines
- Success & cancel callbacks
- Store session URL and session ID
- Calculate total rental price and overdue fines

### Notifications (Telegram)
- New rental notifications
- Successful payment notifications
- Daily scheduled check for overdue rentals

---

## 🧩 Data Models

### Car
- model
- brand
- type: `SEDAN | SUV | HATCHBACK | UNIVERSAL`
- inventory
- dailyFee

### User
- email
- firstName
- lastName
- password
- role: `CUSTOMER | MANAGER`

### Rental
- rentalDate
- returnDate
- actualReturnDate
- carId
- userId

### Payment
- status: `PENDING | PAID`
- type: `PAYMENT | FINE`
- rentalId
- sessionUrl
- sessionId
- amountToPay

---

## 🧱 Technologies Used

| Technology           | Purpose                |
|---------------------|------------------------|
| Java 17             | Language               |
| Spring Boot         | Application framework  |
| Spring Security (JWT)| Authentication        |
| Spring Data JPA     | Persistence layer      |
| Liquibase           | Database migrations    |
| Stripe API          | Payments               |
| Telegram Bot API    | Notifications          |
| Docker & Docker Compose | Containerization    |
| Swagger / OpenAPI   | Documentation          |

---

## 🗂️ Project Structure

controller/ → REST API controllers
service/ → Business logic
repository/ → Spring Data JPA repositories
model/ → JPA entities & enums
dto/ → Request/Response DTOs
security/ → JWT filters and authentication
config/ → Application configuration
mapper/ → Entity ↔ DTO mappers

---

## 🐳 Running the Application with Docker

1️⃣ Copy `.env.sample` to `.env` and update values:

```bash
cp .env.sample .env
2️⃣ Start containers:
docker-compose up --build
Starts the database
Starts the Spring Boot app
Runs Liquibase migrations
Exposes API at http://localhost:8080/
🔌 Swagger API Documentation
http://localhost:8080/swagger-ui/index.html
Interactive UI for testing all endpoints.
```
## 📦 Postman Collection

To make API testing easy, this project includes a ready-to-use Postman collection.

[Download Postman Collection](./postman/car-sharing-collection.json)


# **👨‍💻 Author**

Anastasiia Beshleha (Abeshle)

📧 anastasiiabeshleha@gmail.com 

If you find this project useful — ⭐ star the repository!