# 🧭 Workmap — Backend Spring Boot SaaS RH & Commercial

**Projet :** Développement d’un SaaS RH & Commercial (Missions, Timesheets, Facturation, Reporting) pour ESN & cabinets de conseil.  
**Méthode :** Agile Scrum — sprints de 2 semaines  
**Objectif :** MVP multi-tenant avec gestion missions, timesheets, facturation T&M, reporting, CI/CD, et API REST.

---

## ⚙️ Cadre global

- **Durée totale** : 8 sprints (16 semaines)
- **Architecture** : Modular Monolith Spring Boot (extensible microservices)
- **Branches Git** :
    - `main` (production)
    - `develop` (intégration)
    - `feat/<ticket-id>-short-desc` (features)
- **Stack technique** :
    - Spring Boot 3.x, JPA/Hibernate, Flyway, PostgreSQL
    - Kafka / RabbitMQ, Minio (S3), Docker, Kubernetes, GitHub Actions
    - OpenAPI 3 (Springdoc), Micrometer + OpenTelemetry

---

## 🏁 Sprint 0 — Préparation (1 semaine, chevauchable avec Sprint 1)

### 🎯 Objectif
Mise en place du repo, CI/CD de base, DB initiale, backlog prêt.

### ✅ Tâches
1. **Repo initial + branch strategy**
    - Créer `saas-hr-billing-backend`
    - README, CONTRIBUTING, ISSUE_TEMPLATE, PR_TEMPLATE
    - Branch protection sur `main`

2. **CI Baseline (GitHub Actions)**
    - Build + tests + lint
    - Dockerfile de base

3. **Infra locale (DevOps)**
    - `docker-compose.yml` (Postgres, Kafka, Minio, app)
    - Scripts `./dev up` et `./dev down`

4. **Migrations initiales (Flyway)**
    - V1__init.sql : tenants, users, clients, missions, timesheets, invoices, audit_logs

5. **ADR & architecture**
    - Décision : monolith modulaire + stratégie multi-tenant (shared DB + tenant_id)

6. **Backlog Jira/Trello**
    - Création des epics + priorisation initiale

### 📦 Livrables
- Repo + CI opérationnelle
- Docker-compose fonctionnel
- Flyway baseline
- Backlog prêt

---

## 🧩 Sprint 1 — Authentification & Multi-tenant

### 🎯 Objectif
JWT Auth, gestion tenant/user, sécurité de base.

### ✅ Tâches principales
- Implémentation Spring Security OAuth2 (JWT)
- CRUD Tenant/User
- Filtrage par `tenant_id` dans tous les endpoints
- OpenAPI /api/docs
- Tests unitaires + intégration (Testcontainers)

### 📦 Livrables
- Auth complète (login, refresh)
- Multi-tenant OK
- Docs & tests

---

## 🧭 Sprint 2 — Missions & Affectations

### 🎯 Objectif
Missions, clients, affectations et tarification.

### ✅ Tâches
- CRUD Missions
- CRUD Clients
- Assignments (consultants ↔ missions)
- RateCards par rôle
- Tests E2E (création tenant → mission → assignation)

### 📦 Livrables
- Missions & Assignments opérationnels
- API documentée + validée QA

---

## 🕒 Sprint 3 — Timesheets (MVP)

### 🎯 Objectif
Création et soumission hebdomadaire des timesheets.

### ✅ Tâches
- CRUD TimesheetEntry
- Endpoint `/timesheets/submit`
- Validation heures / semaine
- Événement `TimesheetSubmitted` (Kafka/Rabbit)
- Tests d’intégration

### 📦 Livrables
- Soumission fonctionnelle
- Événements publiés
- Tests OK

---

## ✅ Sprint 4 — Workflow d’approbation & Notifications

### 🎯 Objectif
PM Approval + Audit + Notifications.

### ✅ Tâches
- Endpoints `approve` / `reject`
- AuditLogs sur chaque action
- Service Notifications (email/webhook)
- Événement `TimesheetApproved`

### 📦 Livrables
- Workflow complet (submit → approve → notify)
- Logs audités

---

## 💰 Sprint 5 — Facturation (T&M)

### 🎯 Objectif
Génération automatique des factures à partir des timesheets approuvées.

### ✅ Tâches
- Modèles `invoices`, `invoice_lines`, `payments`
- Endpoint `/invoices/generate`
- Génération PDF (Thymeleaf ou PDFBox)
- Export CSV + marquage payé
- Tests E2E (submit → approve → invoice)

### 📦 Livrables
- Factures PDF stockées
- CSV exports + marquage payé

---

## 📊 Sprint 6 — Reporting & Agrégats

### 🎯 Objectif
Rapports d’utilisation, revenus, performance SQL.

### ✅ Tâches
- Endpoints `/reports/utilization` & `/reports/revenue`
- Vues matérialisées (`mv_timesheet_aggregates`)
- Export CSV
- Indexation + optimisation DB

### 📦 Livrables
- Rapports fiables et performants
- Exports CSV disponibles

---

## 🛡️ Sprint 7 — Sécurité & Durcissement

### 🎯 Objectif
Sécurité, secrets, observabilité, RLS.

### ✅ Tâches
- Audit OWASP + dépendances
- Vérification isolation tenant
- Secrets Manager (Vault)
- Rate limiting (Gateway)
- Micrometer + OpenTelemetry + Jaeger

### 📦 Livrables
- Système stable et sécurisé
- Logs, traces, métriques intégrés

---

## 🚀 Sprint 8 — Release, Onboarding & Docs

### 🎯 Objectif
Mise en production, documentation et onboarding client.

### ✅ Tâches
- Finalisation OpenAPI + Postman
- Script d’onboarding (tenant + CSV import)
- Pipeline release (canary + rollback)
- Runbook: backup, incident
- Démo pilote client

### 📦 Livrables
- Déploiement production
- Runbook complet
- Premier onboarding client réussi

---

## 📋 Backlog priorisé (Jira-ready)

| ID | Tâche | Points | Critères d’acceptation |
|----|-------|---------|------------------------|
| AUTH-01 | Implémenter JWT Auth | 5 | Login renvoie JWT avec claim tenant |
| TENANT-01 | Créer endpoint Tenant | 3 | Tenant créé + plan par défaut |
| USER-01 | Flow d’invitation user | 3 | Token d’invitation valide |
| MISSION-01 | CRUD Mission | 5 | Mission stockée + manager assigné |
| ASSIGN-01 | Créer assignation | 3 | User + mission + taux horaire |
| TIMESHEET-01 | Créer entrée Timesheet | 3 | Status DRAFT |
| TIMESHEET-02 | Soumettre semaine | 5 | Status SUBMITTED + event envoyé |
| APPROVAL-01 | Approuver Timesheet | 3 | Status APPROVED + audit |
| BILL-01 | Générer facture | 8 | PDF stocké + total correct |
| REPORT-01 | Rapport utilisation | 5 | % utilisation correct |
| CI-01 | CI pipeline | 3 | PR → build + test |
| OPS-01 | Deploy staging | 5 | Smoke test OK |

---

## 🧾 Definition of Done

- PR approuvée et mergée
- Tests unitaires et d’intégration ajoutés
- OpenAPI mis à jour
- CI/CD vert
- Déployé sur staging
- PO valide les critères

---

## 🔀 Conventions Git

- **Branch** : `feat/JIRA-123-add-timesheet-submit`
- **PR title** : `[JIRA-123] Add timesheet submit endpoint`
- **Commit message** : `JIRA-123: implement POST /timesheets/submit with validation`

---

## 🧪 CI/CD Pipeline

1. **PR → develop**
    - `mvn test`, static analysis, Docker build/tag `develop-<sha>`
2. **Merge → main**
    - Build complet + tests intégration
    - Tag `vX.Y.Z` + push Docker image
    - Deploy staging → prod (manuel)
3. **Health Check**
    - `/actuator/health` (smoke test)

---

## 📈 Monitoring & Alerts

- **Metrics :** latency, error rate, DB usage, consumer lag
- **Dashboard Grafana :**
    - Latency 95th percentile
    - Invoice generation success rate
- **Alertes :**
    - Error rate >2% → PagerDuty
    - Kafka lag > threshold → Slack

---

## ⚠️ Risques & Mitigations

| Risque | Mitigation |
|---------|-------------|
| Bugs de facturation | Jeu de données de référence + audits |
| Fuite inter-tenant | RLS + tests d’isolation |
| Retard SSO | Mock IdP + fallback local |

---

## 🪶 Post-release plan

1. Scinder le monolithe (billing/timesheet/reporting) → microservices
2. Migration par Strangler pattern
3. Ajouter SSO, dashboards analytiques, intégration CRM

---

## 📂 Livrables optionnels disponibles

- Backlog exportable (CSV / Jira JSON)
- Template GitHub Actions CI + Docker Compose + Flyway
- Starter Spring Boot scaffold (Java ou Kotlin)

👉 Dis-moi lequel tu veux que je génère maintenant :
- `1️⃣ Backlog CSV`
- `2️⃣ CI + Docker Setup`
- `3️⃣ Spring Boot Starter`
- ou **tout** ensemble dans un pack complet.
