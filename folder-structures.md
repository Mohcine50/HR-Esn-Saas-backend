saas-rh-commercial/
├── pom.xml
├── README.md
├── .gitignore
├── docker-compose.yml
│
└── src/
├── main/
│   ├── java/com/company/saasrhcommercial/
│   │   ├── SaasRhCommercialApplication.java
│   │   │
│   │   ├── config/                 # Global configuration (CORS, Swagger, Feign, etc.)
│   │   ├── security/               # JWT, filters, authentication, roles
│   │   ├── shared/                 # Common base classes, utils, exceptions, annotations
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── mapper/
│   │   │   ├── model/
│   │   │   ├── response/
│   │   │   └── util/
│   │   │
│   │   ├── modules/                # Business domains (DDD-style modules)
│   │   │   ├── auth/
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   ├── dto/
│   │   │   │   └── mapper/
│   │   │   │
│   │   │   ├── hr/
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   ├── dto/
│   │   │   │   └── mapper/
│   │   │   │
│   │   │   ├── mission/
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   ├── dto/
│   │   │   │   └── mapper/
│   │   │   │
│   │   │   ├── timesheet/
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   ├── dto/
│   │   │   │   └── mapper/
│   │   │   │
│   │   │   ├── billing/
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   ├── dto/
│   │   │   │   └── mapper/
│   │   │   │
│   │   │   └── reporting/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── entity/
│   │   │       ├── dto/
│   │   │       └── mapper/
│   │   │
│   │   └── infrastructure/         # (optional) Adapters: mail, storage, kafka, etc.
│   │       ├── mail/
│   │       ├── storage/
│   │       ├── integration/
│   │       └── scheduler/
│   │
│   └── resources/
│       ├── application.yml
│       ├── db/migration/           # Flyway migrations (V1__init.sql, etc.)
│       └── logback-spring.xml
│
└── test/
└── java/com/company/saasrhcommercial/
├── unit/
└── integration/


| Module        | Description                                         | Key Entities                |
| ------------- | --------------------------------------------------- | --------------------------- |
| **auth**      | User management, authentication, roles, permissions | User, Role, Permission      |
| **hr**        | Employee records, contracts, leaves                 | Employee, Contract, Leave   |
| **mission**   | Client missions, consultant assignments             | Mission, Client, Consultant |
| **timesheet** | Work hours, validation workflow                     | Timesheet, TimesheetEntry   |
| **billing**   | Invoicing, payments, accounting sync                | Invoice, Payment            |
| **reporting** | KPIs, dashboards, performance data                  | Report, Metric              |

