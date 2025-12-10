# Finance Assistant Backend

A Spring Boot backend API for personal finance management.  
It lets a user upload transaction CSVs, stores them in PostgreSQL, and exposes rich REST APIs for CRUD operations and analytics (spending breakdown, summaries, budgets, savings rate, etc.).

---

## Features

### 1. CSV Ingestion

- Upload a CSV file with transactions.
- Parse and validate rows (date, description, category, amount, type).
- Persist valid transactions into PostgreSQL.

**Endpoint:**

- `POST /api/files/upload`  
  - Body: `multipart/form-data` with `file` field.
  - Response: JSON with `status` and `transactionsProcessed`.

Expected CSV columns (header row):

- `date` (format: `yyyy-MM-dd`)
- `description`
- `category`
- `amount`
- `type` (`INCOME` or `EXPENSE`)

Example row:

2025-01-15,Whole Foods,Groceries,89.99,EXPENSE


---

### 2. Transactions CRUD API

Core REST endpoints to manage individual transactions.

**Model:** `Transaction`

- `id` (Long, auto-generated)
- `date` (LocalDate)
- `description` (String)
- `category` (String)
- `amount` (BigDecimal)
- `type` (String: `INCOME` or `EXPENSE`)

**Endpoints:**

- `GET /api/transactions`  
  - Query params (optional):
    - `type` – `INCOME` or `EXPENSE`
    - `category` – e.g. `Groceries`, `Food`
    - `page` – page index (default `0`)
    - `size` – page size (default `10`)
    - `sortBy` – field (`date`, `amount`, `description`, `category`, `type`)
    - `direction` – `asc` or `desc`
  - Returns:
    - `status`
    - `page`, `size`
    - `totalElements`, `totalPages`
    - `transactions` (list)

- `GET /api/transactions/{id}`  
  - Returns single transaction or `404` if not found.

- `POST /api/transactions`  
  - Body (JSON):
    ```
    {
      "date": "2025-12-10",
      "description": "Amazon Purchase",
      "category": "Shopping",
      "amount": 150.50,
      "type": "EXPENSE"
    }
    ```
  - Validations:
    - `date` required
    - `amount` > 0
    - `type` must be `INCOME` or `EXPENSE`
  - Returns created transaction with `201 Created`.

- `PUT /api/transactions/{id}`  
  - Partial update supported (only send fields to change).
  - Validates `amount` (> 0) and `type` (if present).
  - Returns updated transaction or `404` if not found.

- `DELETE /api/transactions/{id}`  
  - Deletes by id.
  - Returns success or `404` if not found.

---

### 3. Analytics API

High-level financial insights computed from all stored transactions.

**Endpoints:**

- `GET /api/analytics/summary`  
  Returns:
  - `totalIncome`
  - `totalExpense`
  - `netBalance`
  - `transactionCount`
  - `savingsPercentage`

- `GET /api/analytics/by-category`  
  Returns:
  - `categoryBreakdown` – map of `category -> totalExpense`.

- `GET /api/analytics/daily?date=YYYY-MM-DD`  
  If `date` omitted, uses today. Returns:
  - `income`, `expense`, `netDaily`
  - `transactionCount`
  - `transactions` for that day.

- `GET /api/analytics/range?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`  
  Returns:
  - `income`, `expense`, `netBalance`
  - `transactionCount`
  - `averageDailyExpense`

- `GET /api/analytics/monthly?year=YYYY&month=MM`  
  If omitted, uses current year/month. Returns:
  - `income`, `expense`, `netSavings`
  - `yearMonth`
  - `categoryBreakdown` for expenses
  - `transactionCount`.

- `GET /api/analytics/comparison`  
  Returns:
  - `income`, `expense`, `balance`
  - `incomePercentage`, `expensePercentage`.

- `GET /api/analytics/top-expenses?limit=N`  
  Returns top N expense transactions sorted by amount (desc).

- `GET /api/analytics/trends?category=CategoryName`  
  Returns:
  - `totalExpenses`
  - `transactionCount`
  - `averageExpense`
  - `category` (or “All Categories” if not specified).

- `POST /api/analytics/budget-check`  
  Body example:


{ “Groceries”: 200, “Entertainment”: 100, “Food”: 50 }


Returns budget vs actual per category, plus totals and utilization.

- `GET /api/analytics/savings-rate`  
Returns:
- `totalIncome`, `totalExpense`, `netSavings`
- `savingsRate` (percentage)
- `savingsRateCategory` (e.g. `Excellent`, `Good`).

---

## Tech Stack

- **Language:** Java
- **Framework:** Spring Boot
- **Persistence:** Spring Data JPA, Hibernate
- **Database:** PostgreSQL
- **Build Tool:** Maven
- **Logging:** SLF4J + Lombok (`@Slf4j`)
- **Other:** Lombok (`@RequiredArgsConstructor`)

---


---

## Running the Project

1. **Configure PostgreSQL**

Create database (example):

CREATE DATABASE finance_scheduler_db;


Update `application.properties` (or `application.yml`) with your DB credentials, for example:

spring.datasource.url=jdbc:postgresql://localhost:5432/finance_scheduler_db 
spring.datasource.username=your_user spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update 
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect


2. **Build and run**

mvn clean install
mvn spring-boot:run


API will run on `http://localhost:8081` (or your configured port).

3. **Health Check**

GET /api/files/health


Response: `Finance Assistant Healthy`

---

## Example Usage Flow

1. **Upload CSV**
   - `POST /api/files/upload` with `test-transactions.csv`.

2. **List transactions (paginated)**
   - `GET /api/transactions?page=0&size=10`

3. **Filter expenses**
   - `GET /api/transactions?type=EXPENSE&page=0&size=10`

4. **Get analytics summary**
   - `GET /api/analytics/summary`

5. **Check budget**
   - `POST /api/analytics/budget-check` with per-category budget JSON.

---





