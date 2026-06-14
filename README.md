# Сервис учета личных и семейных финансов

Backend-система и frontend-интерфейс для учета доходов, расходов, семейных групп, категорий и отчетов.

## Архитектура

Проект реализован как Maven multi-module с микросервисами:

- `auth-service` (`8081`) - регистрация, вход, выдача JWT.
- `finance-service` (`8082`) - группы, участники, категории, операции, проверка доступа.
- `reports-service` (`8083`) - отчеты и аналитика через REST-вызов `finance-service`.
- `frontend` (`8080`) - web-интерфейс для работы с системой.

В Docker Compose используются две PostgreSQL БД:

- `auth-db` (`5433`) - пользователи.
- `finance-db` (`5434`) - группы, категории и операции.

H2 оставлен как fallback: если запускать `auth-service` или `finance-service` без Docker и без переменных datasource, сервисы стартуют на встроенной H2.

## Технологии

- Java 17
- Spring Boot 3
- Spring MVC
- Spring Security + JWT
- Spring Data JPA
- Bean Validation
- PostgreSQL
- H2 для локального fallback-режима
- Springdoc OpenAPI / Swagger UI
- JUnit 5 / Mockito
- Docker Compose
- HTML/CSS/JavaScript frontend

## Запуск

Собрать jar-файлы:

```bash
mvn clean package
```

Запустить всю систему:

```bash
docker compose up --build
```

Frontend:

- http://localhost:8080

Swagger UI:

- Auth: http://localhost:8081/swagger-ui.html
- Finance: http://localhost:8082/swagger-ui.html
- Reports: http://localhost:8083/swagger-ui.html

## Что можно делать во frontend

- регистрироваться и входить;
- создавать семейные или финансовые группы;
- добавлять участников в группу;
- создавать категории доходов и расходов;
- добавлять доходы и расходы;
- смотреть историю операций;
- строить отчеты;
- смотреть аналитику расходов по категориям;
- смотреть динамику по месяцам;
- скачивать CSV-отчет.

## Основной сценарий

1. Открыть `http://localhost:8080`.
2. Зарегистрироваться или войти.
3. Создать группу во вкладке `Группы`.
4. Создать категории во вкладке `Категории`.
5. Добавить доходы и расходы во вкладке `Операции`.
6. Смотреть сводку на вкладке `Обзор`.
7. Строить отчеты на вкладке `Отчеты`.

После создания группы frontend автоматически выбирает ее в формах категорий, операций и отчетов. После создания категории она сразу появляется в списке выбора операции.

## API

Регистрация:

```http
POST http://localhost:8081/api/auth/register
```

Вход:

```http
POST http://localhost:8081/api/auth/login
```

Группы:

```http
POST http://localhost:8082/api/groups
GET  http://localhost:8082/api/groups
POST http://localhost:8082/api/groups/{groupId}/members
```

Категории:

```http
POST http://localhost:8082/api/categories
GET  http://localhost:8082/api/categories?groupId=1
```

Операции:

```http
POST http://localhost:8082/api/operations
GET  http://localhost:8082/api/operations?groupId=1
```

Отчеты:

```http
GET http://localhost:8083/api/reports/summary?from=2026-01-01&to=2026-01-31&groupId=1
GET http://localhost:8083/api/reports/expense-analytics?from=2026-01-01&to=2026-01-31&groupId=1
GET http://localhost:8083/api/reports/summary.csv?from=2026-01-01&to=2026-01-31&groupId=1
```

## Переменные окружения

- `JWT_SECRET` - общий секрет подписи JWT.
- `INTERNAL_TOKEN` - токен для внутренних запросов между `reports-service` и `finance-service`.
- `FINANCE_SERVICE_URL` - адрес `finance-service` для `reports-service`.
- `SPRING_DATASOURCE_URL` - JDBC URL PostgreSQL.
- `SPRING_DATASOURCE_USERNAME` - пользователь PostgreSQL.
- `SPRING_DATASOURCE_PASSWORD` - пароль PostgreSQL.

## Тесты

```bash
mvn test
```
