# Ledger Pay

**Payment Processing & Double-Entry Ledger Backend**

Ledger Pay is a backend payment processing system built with Java and Spring Boot. It integrates with Paystack to process payment notifications, verify transactions, manage payment states, and record financial movements using double-entry accounting.

The project explores how reliable payment infrastructure handles asynchronous events, retries, duplicate requests, and financial consistency.

## Architecture

```text
Client
  |
  v
Payment API
  |
  v
Paystack
  |
  v
Webhook Endpoint
  |
  v
Signature & Payload Validation
  |
  v
Webhook Audit + Outbox
  |
  v
Kafka
  |
  v
Webhook Consumer
  |
  v
Paystack Transaction Verification
  |
  v
Payment Status Update
  |
  v
Journal Posting
  |
  v
PostgreSQL Ledger
```

## Key Features

- **Payment management:** Tracks payment records and their lifecycle.
- **Paystack integration:** Receives webhook notifications and verifies transactions against Paystack.
- **Webhook security:** Validates webhook signatures before accepting events.
- **Transactional outbox:** Persists webhook events before publishing them to Kafka.
- **Asynchronous processing:** Uses Kafka to decouple webhook ingestion from processing.
- **Idempotency:** Prevents duplicate webhook ingestion and protects payment and journal processing against repeated events.
- **Webhook auditing:** Tracks event status, processing attempts and failures.
- **Double-entry accounting:** Records financial movements through balanced debit and credit entries.

## Double-Entry Ledger

For a successful payment of ₦5,000:

| Account | Debit | Credit |
|---|---:|---:|
| Provider Clearing | ₦5,000 | — |
| Merchant Payable | — | ₦5,000 |

Every journal entry must balance:

`Total Debits = Total Credits`

## Technology Stack

- Java 17
- Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL
- Apache Kafka
- Flyway
- Docker
- Paystack API

## Running Locally

**Prerequisites**

- Java 17+
- Docker and Docker Compose
- A Paystack test account and secret key

**1. Clone the repository**

```bash
git clone <repository-url>
cd ledger-pay
```

**2. Configure environment variables**

Create a `.env` file using the variables required by your application:

```env
POSTGRES_USER=your_username
POSTGRES_PASSWORD=your_password
POSTGRES_DB=ledger_pay_db

PAYSTACK_SECRET_KEY=your_paystack_test_secret
```

Configure any additional application properties required by your local setup. Never commit real credentials.

**3. Start PostgreSQL**

```bash
docker compose up -d
```

**4. Run the application**

```bash
./mvnw spring-boot:run
```

Flyway manages database migrations during application startup.

## Testing

The project includes tests for payment processing, journal posting, and webhook-related behaviour.

End-to-end testing has also been used to verify the flow from Paystack webhook reception through Kafka processing, transaction verification, payment status updates, and journal posting.

## Current Development

Ledger Pay is an evolving backend engineering project.

Areas of continued development include:

- More comprehensive integration and failure-recovery testing.
- Retry policies and dead-letter handling.
- Reconciliation workflows.
- Stronger concurrency protection.
- Production deployment and observability.

---

**Built to explore the engineering behind reliable payment processing and financial record-keeping.**