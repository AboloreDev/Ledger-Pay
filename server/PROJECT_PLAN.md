# Development Environment
Vscode
Docker
Contabo VPS
Maven
SpringBoot
Git and Github
JDK 21
PostgreSQL

# The scenario
Imagine a fictional Nigerian marketplace called MarketHub. A customer buys goods worth ₦100,000 from a seller.
1. MarketHub requests payment through Paystack.
2. Paystack processes the customer's payment.
3. Our application verifies the payment outcome.
4. Our ledger records how much the marketplace owes the seller and any applicable fees.
5. Later, our application calculates a simulated settlement and reconciles its records against the provider's report.
Our application does not receive, store or transfer real customer funds. Paystack's sandbox lets us demonstrate the integration without real payments.
Why is our application needed?
Paystack can report that a payment succeeded. But MarketHub still needs to answer questions such as:
- Which seller is entitled to the money?
- How much is the seller owed after fees?
- Has this payment already been recorded?
- What happens if Paystack sends the same webhook twice?
- Does our record agree with the provider's transaction report?
Our application is the financial-records and payment-operations layer that helps answer those questions.

# REQUIREMENTS

A. Functional requirements
These describe what the system must be able to do.
ID	Requirement
FR-01	Register and manage merchants.
FR-02	Create a payment request for a merchant.
FR-03	Track payment status, such as pending, successful or failed.
FR-04	Prevent duplicate processing when a request is retried.
FR-05	Record financial transactions using double-entry accounting.
FR-06	Receive and verify Paystack sandbox webhooks.
FR-07	Record and simulate refunds.
FR-08	Calculate simulated merchant settlements.
FR-09	Compare internal payment records against provider reports.

B. Non-functional requirements
These describe how reliably and safely the system must operate.
- Financial accuracy: every posted journal entry must balance.
- Reliability: retrying a request must not accidentally record the same financial event twice.
- Data integrity: PostgreSQL constraints and transactions protect critical records.
- Security: verify provider webhook signatures and protect sensitive configuration.
- Auditability: posted financial records must not be silently edited or deleted.
- Testability: important business rules must have automated tests.