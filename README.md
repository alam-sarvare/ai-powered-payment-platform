# AI-Powered Payment Processing Platform

A production-oriented payment processing platform built with Spring Boot,
Kafka, Redis, PostgreSQL, Spring AI, Docker, and GitHub Actions.

The platform supports asynchronous payment risk analysis using Kafka and
Spring AI, Redis caching, idempotent payment processing, retry/DLT handling,
JWT-based security, observability, and automated CI/CD.

---

## Architecture

```text
                         Client
                           |
                           v
                  +-------------------+
                  |   Spring Boot API |
                  +---------+---------+
                            |
                +-----------+-----------+
                |                       |
                v                       v
          PostgreSQL                  Redis
          Payment Data             Cache / Risk
                |
                |
                v
          Kafka Producer
                |
                v
       +--------------------+
       | payment-risk-topic |
       +---------+----------+
                 |
                 v
       +---------------------+
       | Kafka Consumer      |
       | Risk Analysis       |
       +----------+----------+
                  |
                  v
             Spring AI
                  |
                  v
          AI Risk Assessment
                  |
           +------+------+
           |             |
           v             v
        Redis       PostgreSQL

Failures
   |
   v
Kafka Retry
   |
   v
Exponential Backoff
   |
   v
Dead Letter Topic
   |
   v
Error Persistence


GitHub
   |
   v
GitHub Actions
   |
   +--> Build
   |
   +--> Test
   |
   +--> Docker Build
   |
   +--> Artifact

| Technology           | Purpose                          |
| -------------------- | -------------------------------- |
| Java 21              | Backend development              |
| Spring Boot          | Application framework            |
| Spring Security      | Authentication & authorization   |
| JWT                  | API security                     |
| Spring Data JPA      | Database access                  |
| PostgreSQL           | Primary database                 |
| Apache Kafka         | Asynchronous event processing    |
| Redis                | Caching                          |
| Spring AI            | AI-powered payment risk analysis |
| Docker               | Containerization                 |
| GitHub Actions       | CI/CD                            |
| JUnit 5              | Unit testing                     |
| Mockito              | Mock-based testing               |
| Swagger/OpenAPI      | API documentation                |
| Spring Boot Actuator | Monitoring & health checks       |
