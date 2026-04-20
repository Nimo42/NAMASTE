🌿 NAMASTE Terminology Backend
Unified Clinical Terminology Service for AYUSH × ICD-11 (TM2 + Biomedicine)
<p align="center"> <img src="https://user-images.githubusercontent.com/74038190/212284136-03988914-d899-44b4-b1d9-4eeccf656e44.gif" width="650" alt="banner"/> </p> <p align="center"> <img src="https://img.shields.io/badge/Java-21%2B-red?style=flat-square&logo=openjdk" /> <img src="https://img.shields.io/badge/Spring%20Boot-4.x-brightgreen?style=flat-square&logo=springboot" /> <img src="https://img.shields.io/badge/PostgreSQL-18-blue?style=flat-square&logo=postgresql" /> <img src="https://img.shields.io/badge/Maven-Build-orange?style=flat-square&logo=apachemaven" /> <img src="https://img.shields.io/badge/License-MIT-yellow?style=flat-square" /> <img src="https://img.shields.io/badge/Status-Under%20Development-purple?style=flat-square" /> </p>
🧘‍♂️ Overview

NAMASTE Terminology Backend is a Spring Boot powered terminology service that unifies AYUSH (NAMASTE) concepts with ICD-11 Traditional Medicine (TM2) and ICD-11 Biomedicine.


It provides:

🔎 Fast full-text code search

🏷️ Unified concept representation

🔗 Cross-system mappings

📥 CSV import for bulk terminology uploads

🗄️ PostgreSQL persistence with JPA/Hibernate

🛠️ Clean modular architecture

Perfect for clinical decision support, terminology browsers, or AYUSH × ICD interoperability systems.

🧱 Backend Architecture Overview
flowchart TD

A[Client / UI] --> B[REST API Layer<br/>Spring Web Controller]

B --> C[Service Layer<br/>Business Logic]
C --> D[Repository Layer<br/>Spring Data JPA]

D --> E[(PostgreSQL Database)]

C --> F[Mapping Engine<br/>Concept Linking]
C --> G[CSV Import Processor]

B --> H[Search Endpoint<br/>Unified Suggestions]

✨ Features
📚 Code System Management

Create & manage terminology systems

Handles NAMASTE, ICD11_TM2, ICD11_BIOMED, etc.

🧩 Concept Storage

Code, name, description, status

Linked to respective code system

Search-optimized

🔗 Concept Mappings

Mapping types: Equivalent / Narrower / Broader

Confidence scoring

Bi-directional linking

🔍 Powerful Full-Text Search

Search by name, code, keywords

Unified result format

Lightweight & fast

📥 CSV Bulk Import

Upload concepts per code system

Validate data & auto link

Ideal for terminology bootstrapping

🛠️ Tech Stack
<p align="left"> <!-- Java --> <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg" width="45" height="45"/>&nbsp; <!-- Spring Boot -->

<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg" width="45" height="45"/> 

<!-- PostgreSQL -->

<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/postgresql/postgresql-original.svg" width="45" height="45"/> 

<!-- Maven -->

<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/maven/maven-original.svg" width="45" height="45"/> 

<!-- Hibernate -->

<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/hibernate/hibernate-original.svg" width="45" height="45"/> 

<!-- Git -->

<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/git/git-original.svg" width="45" height="45"/> 

<!-- Docker -->

<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original.svg" width="45" height="45"/> 

<!-- Linux -->

<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/linux/linux-original.svg" width="45" height="45"/> 

<!-- IntelliJ -->

<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/intellij/intellij-original.svg" width="45" height="45"/> 

</p>
🚀 Getting Started
✅ Prerequisites

Java 21+

Maven

PostgreSQL 18

Database created:

CREATE DATABASE ayush_terminology;

⚙️ Configuration

src/main/resources/application.properties:

spring.application.name=namaste

spring.datasource.url=jdbc:postgresql://localhost:5433/ayush_terminology
spring.datasource.username=postgres
spring.datasource.password=hehedb
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

server.port=8080
server.servlet.context-path=/


💡 If using data.sql, make inserts idempotent with:
ON CONFLICT DO NOTHING

▶️ Run the Application
mvn spring-boot:run


Runs at:
👉 http://localhost:8080

🧬 Domain Model
CodeSystem

id

name

version

isActive

Concept

id

code

displayName

description

codeSystem

isActive

Mapping

fromConcept

toConcept

mappingType

confidence

Relationships:

1 CodeSystem → Many Concepts

1 Concept → Many Mappings

🌐 REST Endpoints
🔍 Code Search
GET /api/v1/codes/search?q={term}&limit={n}


Example:

GET /api/v1/codes/search?q=diabetes&limit=10


Response Format:

{
  "suggestions": [
    {
      "baseConcept": {
        "id": 1,
        "code": "N456",
        "displayName": "Madhumeha",
        "codeSystem": "NAMASTE"
      },
      "mappings": [
        {
          "codeSystem": "ICD11_BIOMED",
          "code": "5A11",
          "mappingType": "EQUIVALENT",
          "confidence": 0.95
        }
      ]
    }
  ],
  "count": 1
}

📥 CSV Import (Admin)
POST /api/v1/admin/code-systems/{id}/concepts/import


multipart/form-data

file: CSV (code, displayName, description)

🔐 Security

Current dev config:

http.csrf(csrf -> csrf.disable())
    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());


Future:

JWT authentication

Role-based access

Admin-restricted imports

🌱 Database Seeding

Example:

INSERT INTO code_systems (name, version, is_active)
VALUES ('NAMASTE', '1.0', true)
ON CONFLICT (name) DO NOTHING;

🧭 Development Notes

Entities → domain/

Repositories → repository/

Services → service/

Controllers → controller/

Use Postman / Hoppscotch

Keep imports/mappings idempotent

❤️ Contributions

PRs, ideas & improvements are welcome!

🙏 NAMASTE — towards unified clinical understanding.
