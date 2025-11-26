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

🗂️ Data Model Diagram (ER Diagram)
``````
erDiagram
    CAR {
        UUID id PK
        string model
        string brand
        ENUM type "SEDAN | SUV | HATCHBACK | UNIVERSAL"
        int inventory
        decimal dailyFee
    }

    USER {
        UUID id PK
        string email
        string firstName
        string lastName
        string passwordHash
        ENUM role "CUSTOMER | MANAGER"
    }

    RENTAL {
        UUID id PK
        datetime rentalDate
        datetime returnDate
        datetime actualReturnDate NULL
        UUID carId FK
        UUID userId FK
        boolean active
    }

    PAYMENT {
        UUID id PK
        ENUM status "PENDING | PAID"
        ENUM type "PAYMENT | FINE"
        UUID rentalId FK
        string sessionUrl NULL
        string sessionId NULL
        decimal amountToPay
        datetime createdAt
    }

    CAR ||--o{ RENTAL : "has many"
    USER ||--o{ RENTAL : "has many"
    RENTAL ||--o{ PAYMENT : "has many"

``````

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

## 🔁 How to Clone and Run the Project
1️⃣ Clone the repository\
git clone https://github.com/abeshle/car-sharing-app.git \
cd <repo>

2️⃣ Configure environment variables\
Copy the example file:\
cp .env.sample .env\
Edit .env and provide your values (DB, JWT secret, Stripe keys, Telegram bot token, etc.).

3️⃣ Run with Docker\
docker-compose up --build\
This will:\
Start PostgreSQL\
Start the Spring Boot app\
Run Liquibase migrations\
API will be available at:
http://localhost:8080/ \
Swagger UI:
http://localhost:8080/swagger-ui/index.html
## 📦 Postman Collection

To make API testing easy, this project includes a ready-to-use Postman collection.

[Download Postman Collection](./postman/car-sharing-collection.json)


# **👨‍💻 Author**

Anastasiia Beshleha (Abeshle)

📧 anastasiiabeshleha@gmail.com 

If you find this project useful — ⭐ star the repository!