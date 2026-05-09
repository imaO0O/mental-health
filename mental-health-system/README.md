# Система психологического тестирования

Веб-приложение для тестирования психоэмоционального состояния студентов.

## Технологии

- Java 22
- Spring Boot 3.2.0
- Spring Security
- Spring Data JPA
- PostgreSQL
- Thymeleaf
- Bootstrap 5

## Быстрый старт

### 1. Установка PostgreSQL

Установите PostgreSQL и создайте базу данных:

```sql
CREATE DATABASE mental_health_db;
```

### 2. Настройка подключения

Отредактируйте `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mental_health_db
spring.datasource.username=postgres
spring.datasource.password=ваш_пароль
```

### 3. Запуск приложения

```bash
mvnw.cmd spring-boot:run
```

Или через IDE — запустите класс `MentalHealthSystemApplication`.

### 4. Вход в систему

Откройте браузер: http://localhost:8080

**Тестовые учётные данные:**

| Роль | Логин | Пароль |
|------|-------|--------|
| Администратор | admin | admin123 |
| Студент | student | student123 |
| Психолог | psychologist | psychologist123 |

## Функционал

### Студент
- Просмотр доступных тестов
- Прохождение тестирования
- Просмотр результатов и истории
- Получение рекомендаций

### Психолог
- Просмотр результатов студентов
- Создание и редактирование тестов
- Добавление вопросов и ответов
- Формирование рекомендаций
- Статистика по тестам

### Администратор
- Управление пользователями
- Управление тестами
- Общая статистика системы

## Структура проекта

```
src/main/java/ru/rrtu/mental_health_system/
├── config/          # Конфигурация безопасности
├── controller/      # MVC контроллеры
├── dto/             # Data Transfer Objects
├── model/           # JPA Entity
├── repository/      # Репозитории
└── service/         # Бизнес-логика
```

## API Endpoints

- `GET /` — Главная страница (редирект на /login)
- `GET /login` — Страница входа
- `GET /register` — Страница регистрации
- `GET /student/dashboard` — Панель студента
- `GET /psychologist/dashboard` — Панель психолога
- `GET /admin/dashboard` — Панель администратора
