# Bank Management System + Smart Password Generator

An enterprise-grade academic web application combining a full-featured **Bank Management System** and a client-side **Smart Password Generator**, built with Java 21, Spring Boot, Spring Data JPA, MySQL 8, BCrypt encryption, and Vanilla JavaScript.

---

## 📁 Complete Folder Structure

```text
BankManagementSystem/
│
├── frontend/
│   ├── index.html         # Authentication Page (Login & Signup Tabs)
│   ├── home.html          # Authenticated Service Selection Dashboard
│   ├── bank.html          # Customer Bank Dashboard (Balance, Profile, Update, Delete)
│   ├── password.html      # Independent Cryptographic Password Generator Page
│   │
│   ├── style.css          # Shared Enterprise Banking CSS Tokens & Layouts
│   ├── script.js          # Authentication, Redirection, Pre-filled Password Bridge
│   ├── bank.js            # Customer Profile Fetching, Updates, Account Deletion
│   └── password.js        # Web Crypto API Password Generator & Strength Analyzer
│
├── backend/
│   ├── pom.xml            # Maven Configuration & Spring Boot Dependencies
│   ├── mvnw.cmd           # Windows Maven Execution Script
│   │
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/
│           │       └── example/
│           │           └── bankmanagement/
│           │               ├── BankManagementApplication.java
│           │               │
│           │               ├── controller/
│           │               │   ├── AuthController.java
│           │               │   └── CustomerController.java
│           │               │
│           │               ├── service/
│           │               │   ├── AuthService.java
│           │               │   ├── CustomerService.java
│           │               │   └── impl/
│           │               │       ├── AuthServiceImpl.java
│           │               │       └── CustomerServiceImpl.java
│           │               │
│           │               ├── repository/
│           │               │   └── CustomerRepository.java
│           │               │
│           │               ├── entity/
│           │               │   └── Customer.java
│           │               │
│           │               ├── dto/
│           │               │   ├── RegisterRequest.java
│           │               │   ├── LoginRequest.java
│           │               │   ├── LoginResponse.java
│           │               │   ├── CustomerResponse.java
│           │               │   └── UpdateCustomerRequest.java
│           │               │
│           │               ├── security/
│           │               │   ├── SecurityConfig.java
│           │               │   └── PasswordConfig.java
│           │               │
│           │               └── exception/
│           │                   ├── CustomerNotFoundException.java
│           │                   ├── DuplicateResourceException.java
│           │                   ├── InvalidCredentialsException.java
│           │                   └── GlobalExceptionHandler.java
│           │
│           └── resources/
│               └── application.properties
│
└── database/
    └── bank_management.sql # MySQL Database Schema Dump
```

---

## 🚀 Technology Stack

| Domain | Technology |
|---|---|
| **Frontend** | HTML5, CSS3 (Vanilla CSS with Design Tokens), Vanilla JavaScript (ES6+) |
| **Backend Framework** | Java 21 LTS, Spring Boot 3.2.5 |
| **Database & ORM** | MySQL 8, Spring Data JPA, Hibernate ORM |
| **Security & Hashing** | BCrypt (`BCryptPasswordEncoder`), Web Crypto API (`crypto.getRandomValues()`) |
| **Build Tool** | Apache Maven / `mvnw.cmd` |
| **Editor / Server** | VS Code, VS Code Live Server (`http://localhost:5500`) |

---

## 🔑 Key Features

### 1. Customer Authentication (Login & Signup)
* **BCrypt Hashing**: Passwords encrypted with adaptive-cost BCrypt before persistence in MySQL. Plaintext passwords are never stored.
* **Auto Account Generation**: Unique bank account numbers generated automatically in backend (e.g. `AC1000001234`).
* **Input Validation**: Frontend and Spring Validation constraints (`@Email`, `@NotBlank`, `@Size`).

### 2. Customer Bank Management
* **Account Overview**: Real-time balance (`₹10,000.00`), Account Number, Account Type (Savings / Current).
* **Customer Details**: View Full Name, Email, Phone, Address, DOB, Username.
* **Update Profile**: Modify contact details (`Full Name`, `Email`, `Phone`, `Address`). Prevents modification of Account Number, Balance, Username, or Password Hash.
* **Account Deletion**: Confirmed deletion removes customer records from MySQL database and redirects to login.

### 3. Smart Password Generator
* **Web Crypto Security**: Uses `crypto.getRandomValues()` and secure Fisher-Yates shuffle algorithm.
* **Dynamic Strength Analyzer**: Evaluates entropy based on length (8-32), uppercase, lowercase, numbers, and special symbols.
* **Use Password Transfer**: One-click transfer (`[ Use This Password ]`) populates the registration form without storing passwords anywhere.

---

## 🛠️ Step-by-Step Setup & Running Guide (Windows)

### Step 1: Start MySQL Database
Ensure MySQL Server is running on your machine, then execute:

```sql
CREATE DATABASE IF NOT EXISTS bank_management;
USE bank_management;
```

*(Alternatively, run the schema file located at `database/bank_management.sql`)*.

---

### Step 2: Set Environment Variable for MySQL Password

Open **PowerShell** and set your MySQL password environment variable:

```powershell
$env:DB_PASSWORD="YOUR_MYSQL_PASSWORD"
```

*Replace `YOUR_MYSQL_PASSWORD` with your actual local MySQL root password.*

---

### Step 3: Start Spring Boot Backend

Navigate to the `backend/` directory in PowerShell / Command Prompt and run:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The Spring Boot backend will start on **`http://localhost:8080`**.

---

### Step 4: Start Frontend Live Server

1. Open VS Code.
2. Open the `frontend/` directory.
3. Right-click `index.html` and select **Open with Live Server**.
4. Access the application in your browser at:
   **`http://localhost:5500/index.html`** (or `http://127.0.0.1:5500/index.html`).

---

## 📡 REST API Endpoints Summary

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/auth/login` | Authenticate customer with BCrypt check | No |
| `POST` | `/api/auth/logout` | Clear session state | No |
| `POST` | `/api/customers/register` | Register new customer & generate account number | No |
| `GET` | `/api/customers/profile` | Retrieve customer profile details | Header `X-Customer-Id` |
| `PUT` | `/api/customers/profile` | Update profile contact information | Header `X-Customer-Id` |
| `DELETE` | `/api/customers/account` | Permanently delete customer account | Header `X-Customer-Id` |

---

## 🧪 Complete Verification & Testing Checklist

Follow this end-to-end verification flow:

1. **Start MySQL** and verify `bank_management` database exists.
2. **Start Backend** using `.\mvnw.cmd spring-boot:run`.
3. **Open Frontend** `index.html` on `http://localhost:5500`.
4. Switch to **Create Account** tab.
5. Click **⚡ Generate Strong Password** to open `password.html`.
6. Adjust length slider (e.g., 20 characters), toggle character sets, observe strength badge change to **Very Strong**.
7. Click **Copy Password**, then click **Use This Password**.
8. Verify you are returned to `index.html` with Password and Confirm Password pre-filled.
9. Complete remaining registration fields (Full Name, Email, Phone, DOB, Address, Username) and click **Register Account**.
10. Check MySQL database `SELECT * FROM customers;`:
    * Verify `password_hash` starts with `$2a$` (BCrypt hash).
    * Verify plaintext password is NOT saved anywhere.
    * Verify `account_number` is formatted as `AC100000XXXX`.
11. Log in with your new username and password.
12. Verify automatic redirection to `home.html` (Service Selection Dashboard).
13. Click **Open Bank** to navigate to `bank.html`.
14. Verify customer details, account number, and default balance (`₹10,000.00`).
15. Click **Update Profile**, change Phone Number and Address, click **Save Changes**.
16. Verify updated details reflect in UI and in MySQL database.
17. Click **Back to Home**, then click **Open Password Generator**.
18. Click **Logout** and verify attempting to visit `bank.html` directly redirects back to `index.html`.
19. Login again, click **Delete Account**, confirm prompt.
20. Verify customer record is deleted from MySQL and session is cleared.

---

## ❓ Troubleshooting & FAQs

* **Error: `Access denied for user 'root'@'localhost'`**:
  Ensure you set `$env:DB_PASSWORD="YOUR_MYSQL_PASSWORD"` in the terminal window where you run `.\mvnw.cmd spring-boot:run`.
* **CORS Error in Browser**:
  Ensure frontend is running on `http://localhost:5500` or `http://127.0.0.1:5500` as defined in `SecurityConfig.java`.
* **Page Redirection loop**:
  Clear browser `localStorage` and `sessionStorage` via Developer Tools (F12) -> Application -> Local Storage.
