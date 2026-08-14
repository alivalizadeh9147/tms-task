# TMS

### Concurrent Account Transaction Management System

A production-oriented account transaction system built with **Java, Spring Boot, PostgreSQL, Redis, Testcontainers, and k6**, designed around **concurrency safety, transactional consistency, idempotency, and high-load processing**.

---

```text
با سلام
پیشاپیش از اینکه فایل توضیحات طولانی ای وجود داره عذرخواهی می کنم
در ادامه توضیحات دقیق تر می باشد اما به طور مختصر
1) تمامی عملیات ها صحیح هستن و یا انجام می شود یا کلا انجام نمی شود
2) برای درخواست های تکراری و شناسه تراکنش تکراری به طور پیش فرض خطا گزارش می شود که
این عملیات قبلا با این شماره تراکنش انجام شده است
3) حساب مبدا و مقصد نباید یکی باشند
4) از معماری شش ضلعی استفاده کردم برای تست پذیری بالا و همچنین
DDD
برای جدا نگه داشتن بیزنس از زیرساخت
5) برای مدیریت کردن تعداد زیاد درخواست ها از پترن
Queue + Worker
استفاده کردم و دیفالت پروژه رو 4 تا وورکر می باشد
6) کار های بیشتری برای بهبود میشد انجام داد از جمله 
async
کردن درخواست ها ولی من به صورت
sync
پیاده سازی کردم


تمامی بیزنس به صورت درخواست های همزمان و تعداد زیاد تست شده اند و اپلیکیشن
atomic, consistent, correct, idempotent, concurrent
می باشد
پایین تر توضیحات دقیق درمورد لود تست داده شده است که بعد از اجرا کردن اپلیکیشن می توانید تست هارا اجرا کنید
این فایل ها نیازمند اکانت آیدی های معتبر می باشد که پس از باز کردن چندین حساب می توانید مقدار دهی کنید و تست را اجرا کنید
تقریبا کل پروژه دارای تست می باشد از جمله
Unit Test, Integration Test, Load Test, Correctness Test
```

---

# 🐳 Running with Docker

The project provides a Docker Compose setup for running the complete application stack:

- Spring Boot application
- PostgreSQL
- Redis

The Docker setup does **not** build the Maven project.

The application is built with Maven first, and Docker is responsible only for running the generated JAR file.

---

## Prerequisites

Make sure the following tools are installed:

- Java 21
- Maven
- Docker
- Docker Compose

Check the installed versions:

```bash
java -version
mvn -version
docker --version
docker compose version
```

---

## 1. Build the Application

From the project root, run:

```bash
mvn clean package -DskipTests
```

This will compile the project and generate the application JAR.

The generated JAR should be available under:

```text
container/target/
```

For example:

```text
container/
└── target/
    └── container-1.0.0.jar
```

---

## 2. Start the Docker Environment

Move into the `container` directory:

```bash
cd container
```

Then build and start the complete environment:

```bash
docker compose up -d --build
```

Docker Compose will start:

```text
┌─────────────────────────────┐
│          TMS App            │
│        Port: 8080           │
└──────────────┬──────────────┘
               │
       ┌───────┴────────┐
       │                │
       ▼                ▼
┌─────────────┐   ┌─────────────┐
│ PostgreSQL  │   │    Redis    │
│ Port: 5432  │   │ Port: 6379  │
└─────────────┘   └─────────────┘
```

The application will be available at:

```text
http://localhost:8080
```

---

## 3. Check Running Containers

Run:

```bash
docker compose ps
```

You should see the following services:

```text
tms-app
tms-postgres
tms-redis
```

To see application logs:

```bash
docker compose logs -f app
```

To see logs for all services:

```bash
docker compose logs -f
```

---

## 4. Stop the Application

To stop all containers:

```bash
docker compose down
```

PostgreSQL and Redis data will remain stored in Docker volumes.

---

## 5. Reset the Environment

To completely remove the containers and their persisted data:

```bash
docker compose down -v
```

This removes:

- Application container
- PostgreSQL container
- Redis container
- PostgreSQL data
- Redis data

The next startup will create a completely fresh environment.

---

## 6. Rebuild After Code Changes

Whenever the source code changes, rebuild the application:

```bash
cd ..
mvn clean package -DskipTests
```

Then return to the `container` directory:

```bash
cd container
```

Rebuild and restart the containers:

```bash
docker compose up -d --build
```

---

## Quick Start

If everything is already configured, the complete process is:

```bash
mvn clean package -DskipTests
cd container
docker compose up -d --build
```

The application will then be available at:

```text
http://localhost:8080
```

---

## Docker Architecture

The Docker environment consists of three services:

```text
                    TMS
                     │
                     ▼
              ┌─────────────┐
              │ Spring Boot │
              │   :8080     │
              └──────┬──────┘
                     │
            ┌────────┴────────┐
            │                 │
            ▼                 ▼
     ┌─────────────┐   ┌─────────────┐
     │ PostgreSQL  │   │    Redis    │
     │   :5432     │   │    :6379    │
     └─────────────┘   └─────────────┘
```

Inside the Docker network, the application connects to:

```text
PostgreSQL:
postgres:5432

Redis:
redis:6379
```

The application should therefore not use `localhost` to connect to PostgreSQL or Redis when running inside Docker.

---

## Docker and Maven

The project intentionally separates the build and runtime environments.

### Maven

Maven is responsible for:

- Compiling the application
- Resolving dependencies
- Running tests
- Generating the JAR

### Docker

Docker is responsible for:

- Providing the Java 21 runtime
- Running the application
- Running PostgreSQL
- Running Redis

The final application image contains only:

```text
Java 21 JRE
+
Application JAR
```

Maven, source code, `.m2`, and build tools are not included in the final runtime image.

---

## Docker and Testcontainers

Docker Compose and Testcontainers have different responsibilities.

Docker Compose is used to run the application:

```text
Docker Compose
│
├── Spring Boot
├── PostgreSQL
└── Redis
```

Testcontainers is used for integration tests:

```text
Testcontainers
│
├── PostgreSQL
└── Redis
```

This keeps the runtime environment separate from the integration-test infrastructure.

---

# Info

If you don't want to use rest-api, use this interface.
```java
public interface AccountService {

    UUID openAccount(String name);

    void credit(UUID accountId,
                long amount,
                String transactionId);

    void debit(UUID accountId,
               long amount,
               String transactionId);

    void transfer(UUID sourceAccountId,
                  UUID destinationAccountId,
                  long amount,
                  String transactionId);

    AccountDto get(UUID accountId);
}
```

---

# Swagger UI

Swagger ui imported to project, see:
```json lines
http://localhost:8080/swagger-ui/index.html
```

---

# 🏗️ Architecture

The project follows a domain-centric architecture inspired by **Hexagonal Architecture / Ports and Adapters**.

```text
                         ┌─────────────────────┐
                         │       REST API      │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │Application Delivery │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │    Idempotency      │
                         │       Redis         │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │   Operation Queue   │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │      Workers        │
                         │   Virtual Threads   │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │       Use Case      │
                         │         DDD         │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │  Repository Port    │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │     PostgreSQL      │
                         └─────────────────────┘
```

---

# 🧰 Tech Stack

| Technology | Purpose |
|---|---|
| Java | Core language |
| Spring Boot | Application framework |
| Spring Data JPA | Persistence |
| PostgreSQL | Primary database |
| Redis | Idempotency |
| Docker | Infrastructure |
| Testcontainers | Integration testing |
| JUnit | Testing |
| Mockito | Mocking |
| AssertJ | Assertions |
| Grafana k6 | Load testing |
| Maven | Build system |

---

# 📦 Project Structure

The project is organized around domain boundaries and application ports.

```text
tms
│
│── core
│   │── domain
│   │
│   └── application
│       │── usecases
│       │── ports
│           │── inbound
│           │── outbound
│
├── adapters
│   ├── primary
│   │   └── rest-api
│   │
│   └── secondary
│       ├── persistence
│       └── redis
│
│── application-delivery
│
└── container
```

The exact package/module structure may evolve, but the main architectural rule remains:

> Business rules should not depend on infrastructure details.

---

# 💰 Domain Model

## Account

The `Account` aggregate is responsible for account state and financial invariants.

Main state:

```text
Account
├── id
├── name
├── balance
└── domain events
```

Main operations:

```java
Account.open(String name);

account.deposit(Money amount);

account.debit(Money amount);
```

The domain owns the rules rather than delegating business logic to controllers or repositories.

---

# 💵 Money

Money is represented by a dedicated value object backed by `BigDecimal`.

Typical operations:

```text
add()
subtract()

isGreaterThan()
isGreaterThanOrEqualTo()

isLessThan()
isLessThanOrEqualTo()

isZero()
```

This prevents monetary calculations from being spread throughout the application.

The domain is responsible for validating monetary invariants such as positive amounts and sufficient balance.

---

# 📡 Domain Events

The `Account` aggregate collects domain events during state changes.

Examples:

```text
AccountOpenedEvent
DepositedMoneyEvent
DebitMoneyEvent
```

Events are collected inside the aggregate and later pulled by the application layer.

```java
account.pullDomainEvents();
```

This keeps the domain independent from Spring and infrastructure implementations.

---

# 🔄 Account Operations

## Open Account

```text
Account.open("Ali")
        │
        ├── Generate Account ID
        ├── Initial balance = 0
        ├── Create Account
        └── AccountOpenedEvent
```

---

## Deposit

```text
Current Balance
       │
       │ + Amount
       ▼
New Balance
```

Example:

```text
1000 + 500 = 1500
```

---

## Debit

A debit requires:

- A positive amount
- Sufficient account balance

```text
Current Balance
       │
       │ - Amount
       ▼
New Balance
```

If there is not enough money:

```text
InsufficientBalanceException
```

is thrown.

---

# 🔄 Transfer

A transfer modifies two accounts atomically.

```text
             Transfer
                │
       ┌────────┴────────┐
       ▼                 ▼
 Source Account     Destination Account
       │                 │
      debit            deposit
       │                 │
       └────────┬────────┘
                ▼
             Commit
```

The transfer is executed within a transaction.

---

# 🔒 Concurrency Control

Concurrency correctness is one of the main goals of the project.

The system uses **PostgreSQL pessimistic locking** for account balance operations.

Conceptually:

```sql
SELECT *
FROM account
WHERE id = ?
FOR UPDATE;
```

The selected row remains locked until the surrounding transaction commits or rolls back.

---

# ❌ Lost Update Problem

Without locking, concurrent requests can produce incorrect balances.

Example:

```text
Initial balance = 1000

Request A reads 1000
Request B reads 1000

A calculates 900
B calculates 900

A writes 900
B writes 900

Final balance = 900 ❌
```

Two debit operations were executed, but only one was reflected in the database.

---

# ✅ With Pessimistic Locking

With row locking:

```text
Request A
   │
   ├── acquire lock
   ├── read 1000
   ├── write 900
   └── commit
            │
            ▼
Request B
   │
   ├── acquire lock
   ├── read 900
   ├── write 800
   └── commit
```

Final balance:

```text
800
```

Both operations are preserved.

---

# 🔒 Transfer Lock Ordering

Transfers require two account rows to be locked.

To reduce deadlock risk, account IDs are sorted before acquiring locks.

```text
Account A ID
Account B ID
      │
      ▼
 Sort IDs
      │
      ▼
Acquire locks in deterministic order
```

Therefore:

```text
Transfer A → B
```

and:

```text
Transfer B → A
```

still acquire locks using the same ordering.

This prevents the classic lock-order inversion problem.

---

# 🗄️ Balance Update

The repository uses a bulk update query:

```java
@Modifying
@Query("""
    update AccountJpaEntity a
       set a.balance = :balance
     where a.id = :id
""")
int updateBalance(
        @Param("id") UUID id,
        @Param("balance") BigDecimal balance
);
```

Because this is a JPQL bulk update, Hibernate's normal dirty checking is bypassed.

---

# 🔢 Entity Versioning

If the entity contains a `@Version` field, a bulk update does **not automatically increment the version** in the same way as a normal entity update.

If version incrementing is required, it should be explicit:

```java
@Modifying
@Query("""
    update AccountJpaEntity a
       set a.balance = :balance,
           a.version = a.version + 1
     where a.id = :id
""")
int updateBalance(
        @Param("id") UUID id,
        @Param("balance") BigDecimal balance
);
```

This is especially important when optimistic locking semantics are also being considered.

---

# 🔐 Idempotency

Financial APIs must protect against duplicate requests.

For example, a client may send:

```http
POST /accounts/{id}/debit
```

The server processes the request, but the client times out.

The client then retries the request.

Without idempotency:

```text
Debit 100
     +
Retry Debit 100
     =
Debit 200 ❌
```

---

# 🛡️ Redis Idempotency

The API accepts:

```http
Idempotency-Key: <unique-key>
```

Redis is used to protect the operation.

```text
             HTTP Request
                  │
                  ▼
             Idempotency
                  │
            ┌─────┴─────┐
            │           │
         Exists       Missing
            │           │
            ▼           ▼
        Duplicate    Execute
                      operation
```

If the key already exists:

```text
DuplicateIdempotencyKeyException
```

is thrown.

---

# ⚡ Concurrent Idempotency

Idempotency was tested under concurrent requests using the same key.

Example:

```text
500 VUs
500 requests
same Idempotency-Key
```

Expected:

```text
1 operation executed
499 duplicate requests rejected
```

The test confirmed that the financial operation is executed only once.

---

# 📥 Operation Queue

Account operations are processed through a bounded queue.

```text
HTTP Request
     │
     ▼
Application Service
     │
     ▼
Operation Queue
     │
     ▼
Worker
     │
     ▼
Use Case
     │
     ▼
Database
```

A bounded queue provides basic backpressure and prevents unlimited in-memory accumulation of operations.

---

# 🧵 Workers

Workers consume operations from the queue.

```text
                  Queue
                    │
        ┌───────────┼───────────┐
        ▼           ▼           ▼
     Worker 1    Worker 2    Worker N
        │           │           │
        └───────────┼───────────┘
                    ▼
                 Use Case
```

---

# 🪶 Virtual Threads

The worker layer uses Java virtual threads.

Virtual threads are useful for this workload because operations may involve blocking calls such as:

- PostgreSQL
- Redis
- Queue operations
- Waiting for asynchronous results

This allows the application to handle a high number of concurrent tasks without creating an equivalent number of heavyweight platform threads.

---

# 🌐 REST API

Example debit endpoint:

```http
POST /accounts/{id}/debit
```

Required header:

```http
Idempotency-Key: <unique-key>
```

Request:

```json
{
  "amount": 100
}
```

Successful response:

```http
200 OK
```

The API returns the standard:

```text
BaseResponse
```

A successful `200 OK` represents a successfully completed operation rather than merely successful queue submission.

---

# 📝 Request Validation

Example transfer request:

```java
@Schema
@Setter
@Getter
public class TransferAccountRestRequest {

    @NotNull
    private UUID destinationAccountId;

    @Positive
    private long amount;

    @NotBlank
    private String transactionId;
}
```

Transport-level validation is performed at the REST boundary.

Business invariants remain inside the domain.

---

# 🧪 Testing Strategy

The project uses multiple testing levels.

```text
                         Testing
                            │
             ┌──────────────┴──────────────┐
             │                             │
        Unit Tests                  Integration Tests
             │                             │
       ┌─────┴─────┐               ┌──────┴──────┐
       │           │               │             │
    Domain     Use Cases      PostgreSQL      Redis
                              Testcontainers Testcontainers
```

Load testing is performed separately using **Grafana k6**.

---

# 🧪 Domain Unit Tests

The domain is tested independently from Spring and infrastructure.

## Account

Covered scenarios:

- Account creation
- Deposit
- Debit
- Insufficient balance
- Domain events

## Money

Covered scenarios:

- Positive values
- Zero
- Negative values
- Addition
- Subtraction
- Comparisons
- Null validation
- Decimal normalization

---

# 🧪 Use Case Unit Tests

Use cases are tested using Mockito.

Repositories are mocked while the real domain model is exercised.

Covered use cases:

```text
CreditMoneyUseCase
DebitMoneyUseCase
OpenAccountUseCase
TransferMoneyUseCase
```

Tests verify:

- Repository interactions
- Domain behavior
- Persistence calls
- Domain event propagation
- Missing account behavior
- Transfer validation
- Multi-account operations

---

# 🐘 Repository Integration Tests

Repository integration tests run against a real PostgreSQL instance using Testcontainers.

Covered scenarios:

```text
Save account
Find account
Find multiple accounts
Find accounts for update
Update balance
Update balance multiple times
```

These tests verify actual persistence behavior rather than mocked repository behavior.

---

# 🔒 Transactional Repository Tests

Queries using:

```sql
FOR UPDATE
```

require an active database transaction.

Without a transaction, PostgreSQL cannot maintain the expected row lock semantics.

Therefore, integration tests for locking behavior must execute inside a transaction.

---

# 🔴 Idempotency Integration Tests

Redis integration tests run against a real Redis container.

Covered scenarios:

```text
Create idempotency key
Reject duplicate key
Concurrent duplicate requests
Ensure only one execution
Verify Redis key
```

This validates the real Redis behavior instead of relying only on mocks.

---

# 🚀 Load Testing

Load tests are implemented using **Grafana k6**.

The tests run against the application through Docker.

Example:

```bash
docker run --rm -i \
  -v "${PWD}:/scripts" \
  grafana/k6 run /scripts/load-test.js
```

We have 3 tests here :
```text
load-test.js
transfer-load-test.js
idempotency-test.js
```

---

# 📊 Load Test Results

The following tests were performed during development.

## 10 VUs — Initial Test

```text
10 VUs
~1,155 requests/sec
```

The initial test exposed an incorrect header value:

```text
Idempotency-Key: null
```

This was fixed by generating a unique idempotency key for every request.

---

## 10 VUs — Corrected

```text
Requests:       1,566
Throughput:     155.9 req/s
HTTP failures:  0%
Checks:         100%
```

---

## 100 VUs

```text
Requests:       5,453
Throughput:     178.9 req/s
HTTP failures:  0%
p95:            ~783 ms
```

---

## 500 VUs

```text
Requests:       6,362
Throughput:     197 req/s
HTTP failures:  0%
p95:            ~2.98 s
```

This demonstrated increased latency under heavy concurrency.

---

## 500 VUs — Improved Configuration

```text
Requests:       11,340
Throughput:     363.7 req/s
HTTP failures:  0%
p95:            ~1.64 s
```

---

## 500 VUs — Four Workers

```text
Requests:       11,663
Throughput:     374.2 req/s
HTTP failures:  0%
p95:            ~1.59 s
```

---

## 500 VUs — Higher Throughput

```text
Requests:       21,767
Throughput:     710.7 req/s
HTTP failures:  0%
p95:            ~804 ms
```

---

## 500 VUs — Stable High Load

```text
Requests:       20,033
Throughput:     651.7 req/s
HTTP failures:  0%
p95:            ~872 ms
```

> These numbers are environment-dependent and should not be considered universal benchmarks.

---

# 🏊 HikariCP

The application uses a database connection pool configuration including:

```yaml
maximum-pool-size: 30
minimum-idle: 10
```

The connection pool becomes an important part of the system's concurrency characteristics.

Increasing workers does not automatically increase throughput.

The actual throughput depends on:

```text
HTTP concurrency
      ↓
Queue capacity
      ↓
Worker count
      ↓
Database connection pool
      ↓
PostgreSQL
      ↓
Transaction duration
      ↓
Row-lock contention
```

---

# 🧪 Concurrent Debit Test

A dedicated concurrent debit test was performed.

### Initial balance

```text
100000
```

### Requests

```text
500 concurrent requests
amount = 100
unique Idempotency-Key per request
```

### Expected

```text
100000 - (500 × 100)
= 50000
```

### Actual

```text
50000
```

This demonstrates that concurrent debit operations did not produce lost updates.

---

# 🔄 Concurrent Transfer Test

Initial state:

```text
Source      = 100000
Destination = 0
```

Requests:

```text
500 concurrent transfers
amount = 100
```

Expected:

```text
Source      = 50000
Destination = 50000
```

Actual:

```text
Source      = 50000
Destination = 50000
```

Total:

```text
50000 + 50000 = 100000
```

No money was created or lost.

This validates an important financial consistency invariant:

> The total amount of money remains constant during transfers.

---

# 🔐 Idempotency Load Test

Scenario:

```text
500 VUs
500 requests
same Idempotency-Key
amount = 100
```

Expected:

```text
1 debit
499 duplicate requests
```

The final balance confirmed that only one debit was executed.

This validates idempotency under real concurrent HTTP traffic.

---

# ✅ Verified Properties

The following properties have been verified:

- Account balances remain consistent under concurrent debit operations.
- Concurrent transfers preserve the total amount of money.
- The same idempotency key cannot execute a financial operation multiple times.
- PostgreSQL row locking prevents lost balance updates.
- Transfer lock ordering provides deterministic lock acquisition.
- The system successfully handles 500 concurrent virtual users in load tests.
- Tested load scenarios completed with zero HTTP failures.
- Concurrent debit results match the mathematically expected balance.
- Concurrent transfers preserve the total amount of money.

---

# 📋 Test Status

| Component | Status |
|---|:---:|
| Money domain | ✅ PASS |
| Account domain | ✅ PASS |
| Domain events | ✅ PASS |
| Credit use case | ✅ PASS |
| Debit use case | ✅ PASS |
| Open account use case | ✅ PASS |
| Transfer use case | ✅ PASS |
| PostgreSQL repository | ✅ PASS |
| PostgreSQL row locking | ✅ PASS |
| Redis idempotency | ✅ PASS |
| Concurrent idempotency | ✅ PASS |
| Concurrent debit | ✅ PASS |
| Concurrent transfer | ✅ PASS |
| Load testing | ✅ PASS |
| Balance consistency | ✅ PASS |
| Money conservation | ✅ PASS |

---

# 📈 Performance Observations

Increasing the number of workers does not increase throughput indefinitely.

Eventually, the bottleneck moves to another component.

Typical bottlenecks include:

```text
HTTP
 │
 ▼
Queue
 │
 ▼
Workers
 │
 ▼
Redis
 │
 ▼
HikariCP
 │
 ▼
PostgreSQL
 │
 ▼
Row Lock Contention
```

For a single heavily accessed account, increasing concurrency eventually increases lock contention instead of increasing useful parallelism.

Transfers can introduce additional contention because two account rows must be locked.

Therefore:

> Performance must always be evaluated together with correctness.

A system that processes more requests but produces incorrect balances is not a successful financial system.

---

# 🎯 Core Design Principles

The project is built around several important principles.

### 1. Business logic belongs to the domain

The domain owns financial invariants.

### 2. Infrastructure should not leak into the domain

PostgreSQL, Redis, HTTP, and Spring-specific concerns stay outside the core domain.

### 3. Financial operations must be atomic

A transfer should either fully succeed or fully fail.

### 4. Concurrent updates must be serialized when necessary

Pessimistic row locking protects account balances from lost updates.

### 5. Retries must be safe

Idempotency prevents duplicate financial operations.

### 6. Performance must not compromise correctness

A higher request rate is meaningless if concurrent transactions produce incorrect balances.

---

# 🏁 Conclusion

TMS is designed around one fundamental requirement:

> **Account balances must remain correct under concurrent financial operations.**

The system combines:

```text
Domain-driven business rules
          +
Transactional application services
          +
PostgreSQL pessimistic locking
          +
Redis idempotency
          +
Bounded operation queue
          +
Virtual-thread workers
          +
Integration testing
          +
Concurrent load testing
```

The implemented tests demonstrate:

```text
Concurrent Debit
       │
       ▼
Correct final balance
```

```text
Concurrent Transfer
       │
       ├── Correct source balance
       ├── Correct destination balance
       └── Constant total money
```

```text
Concurrent Idempotency
       │
       ├── Single execution
       └── Duplicate requests rejected
```

The project has successfully validated its main **transactional consistency, concurrency, idempotency, persistence, and load-testing requirements**.