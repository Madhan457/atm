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
* Customer signup creates a `PENDING` application with a generated account number and zero starting balance.
* Customer and officer sessions use separate server-enforced Spring Security roles.
* Passwords are BCrypt-hashed; customer and officer accounts have no public officer-registration path.

### 2. Customer Bank Management
* Active customers can deposit, withdraw, and transfer simulated funds; each operation is validated by the backend.
* Transfers update both accounts atomically and create matching ledger records.
* Customers can view only their own history, update their profile, and change their password.

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
$env:OFFICER_USERNAME="bank.officer"
$env:OFFICER_PASSWORD="Choose-A-Strong-Password1!"
```

Set these only in the backend launch environment and do not commit real credentials. On first startup, the officer is created once if both officer variables are set and the `officers` table is empty. The password is stored as BCrypt. Existing customers are preserved by the additive schema; existing rows default to `ACTIVE`, while new applications start `PENDING` with a zero balance.

---

### Step 3: Start Spring Boot Backend

Navigate to the `backend/` directory in PowerShell / Command Prompt and run:

```powershell
cd bank-management-system/backend
.\mvnw.cmd spring-boot:run
```

Install Java 21 and Apache Maven first; this repository's `mvnw.cmd` delegates to an installed `mvn` command. The Spring Boot backend listens on **`http://localhost:8080`**.

---

### Step 4: Start Frontend Live Server

1. Open VS Code.
2. Open the `frontend/` directory.
3. Right-click `index.html` and select **Open with Live Server**.
4. Open the Live Server URL. When the workspace root is served, use:
  **`http://localhost:5501/bank-management-system/frontend/index.html`**.
  If the `frontend/` folder itself is served, use **`http://localhost:5500/index.html`**.

---

## 📡 REST API Endpoints Summary

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/auth/customer/login` | Customer username/password login | No |
| `POST` | `/api/auth/officer/login` | Officer username/password login | No |
| `POST` | `/api/auth/logout` | Invalidate the current session | Session |
| `POST` | `/api/customers/register` | Create a pending customer application | No |
| `GET/PUT` | `/api/customers/profile` | Read/update current customer profile | Customer |
| `POST` | `/api/customers/password` | Change password after verification | Customer |
| `POST` | `/api/transactions/{deposit,withdraw,transfer}` | Execute simulated banking operation | Customer |
| `GET` | `/api/transactions/customer/history` | View own transaction history | Customer |
| `GET/PUT` | `/api/officers/customers/**` | Review and manage accounts | Officer |
| `GET` | `/api/officers/dashboard`, `/reports`, `/transactions` | Officer statistics and reports | Officer |

---

## 🧪 Complete Verification & Testing Checklist

Follow this end-to-end verification flow:

1. **Start MySQL** and verify `bank_management` database exists.
2. **Start Backend** using `.\mvnw.cmd spring-boot:run`.
3. **Open Frontend** `frontend/index.html` using Live Server.
4. Choose **Proceed to Bank**, then **Customer Signup**.
5. Click **⚡ Generate Strong Password** to open `password.html`.
6. Adjust length slider (e.g., 20 characters), toggle character sets, observe strength badge change to **Very Strong**.
7. Click **Copy Password**, then click **Use This Password**.
8. Verify you are returned to `customer-auth.html` with Password and Confirm Password pre-filled.
9. Complete remaining registration fields (Full Name, Email, Phone, DOB, Address, Username) and click **Register Account**.
10. Check MySQL database `SELECT * FROM customers;`:
    * Verify `password_hash` starts with `$2a$` (BCrypt hash).
    * Verify plaintext password is NOT saved anywhere.
    * Verify `account_number` is formatted as `AC100000XXXX`.
11. Log in as an officer, approve the pending application, and verify its status changes to `ACTIVE`.
12. Log in as the customer and verify account status, generated account number, and zero balance.
13. Test deposit, withdrawal, transfer, and customer-only transaction history.
14. Test officer search, block/unblock, transaction filters, and reports.
15. Verify pending and blocked customers cannot execute banking operations.
16. Verify the standalone password generator remains available from the public home.

---

## ❓ Troubleshooting & FAQs

* **Error: `Access denied for user 'root'@'localhost'`**:
  Ensure you set `$env:DB_PASSWORD="YOUR_MYSQL_PASSWORD"` in the terminal window where you run `.\mvnw.cmd spring-boot:run`.
* **CORS Error in Browser**:
  Ensure the frontend uses an allowed Live Server origin: `http://localhost:5500`, `http://127.0.0.1:5500`, `http://localhost:5501`, or `http://127.0.0.1:5501`.
* **Page Redirection loop**:
  Clear browser `localStorage` and `sessionStorage` via Developer Tools (F12) -> Application -> Local Storage.
