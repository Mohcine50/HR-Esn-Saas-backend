![HR SaaS Banner](assets/banner.png)

# 🚀 HR-ESN SaaS Backend: Enterprise HR & Billing Solution

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![Architecture](https://img.shields.io/badge/Architecture-Modular%20Monolith-blue.svg)](#-architecture)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A robust, enterprise-grade **Modular Monolith SaaS** designed for ESNs (Entreprise de Services du Numérique) and consulting firms. This platform streamlines HR management, mission tracking, timesheets, and automated billing through a secure multi-tenant architecture.

🔗 **Frontend Repository:** [Explore the Frontend Repo](https://github.com/Mohcine50/HR-saas-and-esn-application/)

---

## 🌟 Key Features

- **🛡️ Multi-Tenancy**: Secure data isolation using a shared database approach with discriminator-based filtering.
- **🔐 Advanced Security**: OAuth2 Resource Server implementation using JWT, asymmetric encryption, and fine-grained RBAC.
- **⏱️ Timesheet Workflow**: Comprehensive work hour tracking with submission and multi-level approval workflows.
- **💰 Automated Billing**: Generation of professional PDF invoices based on approved T&M (Time & Materials) missions.
- **📊 Real-time Analytics**: KPI dashboards for utilization rates, revenue forecasting, and employee performance.
- **📧 Notification Engine**: Event-driven notifications via Email, Webhooks, and WebSockets.
- **☁️ Cloud Native**: Fully containerized with Docker, AWS S3 integration for document storage, and Redis for high-performance caching.

---

## 📸 Screenshots
<div align="center">
  <table>
    <tr>
      <td><img src="https://github.com/user-attachments/assets/81b7e99a-8421-450d-8f28-4167ab8f62f4" width="100%"/></td>
      <td><img src="https://github.com/user-attachments/assets/cf2e4b60-d515-437c-983b-d41c96e1a418" width="100%"/></td>
    </tr>
    <tr>
      <td><img src="https://github.com/user-attachments/assets/33671e63-b37c-4dfb-9b36-9156888e3957" width="100%"/></td>
      <td><img src="https://github.com/user-attachments/assets/d0736d9a-c452-4340-bcde-bc43c33420f8" width="100%"/></td>
    </tr>
    <tr>
      <td><img src="https://github.com/user-attachments/assets/8941003a-3640-4233-bf97-a0de197c60e7" width="100%"/></td>
      <td><img src="https://github.com/user-attachments/assets/feb35ce1-a44d-4231-93a2-8b3f1d3ae9a8" width="100%"/></td>
    </tr>
    <tr>
      <td><img src="https://github.com/user-attachments/assets/fa1e3f4f-4379-4277-9dbf-abcc0ad6da00" width="100%"/></td>
      <td><img src="https://github.com/user-attachments/assets/7ba9c27f-8b51-4572-b4fc-00711a689b1b" width="100%"/></td>
    </tr>
  </table>
</div>

---

## 🏗️ Architecture

The project follows a **Modular Monolith** pattern, balancing the simplicity of a single deployment unit with the decoupling of microservices.

### Core Principles

- **Domain-Driven Design (DDD)**: Logic organized around business domains (Auth, HR, Mission, Billing).
- **Hexagonal Architecture (Ports & Adapters)**: Business logic is decoupled from external infrastructures (DB, API, Mailers).
- **Event-Driven**: Asynchronous communication between modules using **RabbitMQ** to ensure high availability and responsiveness.

### Module Overview

| Module        | Responsibility                 | Key Technologies               |
| :------------ | :----------------------------- | :----------------------------- |
| **Auth**      | Identity & Access Management   | Spring Security, JWT, OAuth2   |
| **HR**        | Employee Lifecycle & Contracts | JPA, Hibernate                 |
| **Mission**   | Client & Project Management    | MapStruct, Validation API      |
| **Timesheet** | Attendance & Approval Flow     | RabbitMQ, Redis                |
| **Billing**   | Invoicing & PDF Generation     | Thymeleaf, OpenHTMLToPDF       |
| **Reporting** | Business Intelligence & KPIs   | PostgreSQL Views, Aggregations |

---

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.x, Spring Data JPA, Spring Security.
- **Database**: PostgreSQL 15, Flyway (Migrations), Redis (Caching).
- **Messaging**: RabbitMQ / AMQP.
- **Storage**: AWS S3 / Minio.
- **Documentation**: OpenAPI 3 (Swagger UI).
- **Testing**: JUnit 5, Mockito, Testcontainers (PostgreSQL, Redis).
- **DevOps**: Docker, Docker Compose, GitHub Actions CI/CD.

---

## 🚀 Getting Started

### Prerequisites

- JDK 17+
- Docker & Docker Compose
- Maven 3.8+

### Installation

1. **Clone the repository**

   ```bash
   git clone https://github.com/Mohcine50/HR-Esn-Saas-backend.git
   cd HR-Esn-Saas-backend
   ```

2. **Spin up Infrastructure**

   ```bash
   docker-compose up -d
   ```

3. **Run the Application**
   ```bash
   ./mvnw spring-boot:run
   ```

### API Access

Once running, the interactive API documentation is available at:
`http://localhost:8080/swagger-ui.html`

---

## 🧪 Quality Assurance

We maintain high engineering standards through:

- **Unit Testing**: Focus on domain logic with 80%+ coverage.
- **Integration Testing**: Validating Persistence and External Integrations using Testcontainers.
- **Static Analysis**: Enforcing Clean Code and SOLID principles.
- **Automated CI**: Every PR undergoes automated builds, tests, and security scans.

---

## 🛣️ Roadmap

- [ ] Transition to Microservices using Spring Cloud Gateway.
- [ ] Implement AI-driven revenue forecasting.
- [ ] Mobile application for employee timesheet entry.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Developed with ❤️ by Mohcine**
_A Senior-level showcase of modern Spring Boot capabilities._
