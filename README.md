# ShopFlow Order Service

Микросервис заказов интернет-магазина электроники **ShopFlow**. Его разрабатывает аутсорс-команда
**DevForge** для заказчика (ООО «ШопФлоу Ритейл», вымышленная компания).

Сервис отвечает за:

- каталог товаров (только чтение и изменение цены; источник правды — PIM заказчика);
- остатки на складе и резервирование;
- корзину покупателя;
- промокоды и расчёт цены;
- оформление заказа (checkout) и его жизненный цикл;
- приём оплаты: банковская карта, **СБП**, **криптовалюта**;
- публикацию событий о заказах в Kafka (их читают сервис уведомлений и сервис доставки).

> **Ты новый разработчик в команде.** Начни с [docs/ONBOARDING.md](docs/ONBOARDING.md):
> там описано, в каком состоянии проект и что от тебя ждут.

## Стек

| Что | Чем |
|---|---|
| Язык | Java 21 |
| Фреймворк | Spring Boot 4.1 (Web MVC, Data JPA, Validation, Cache, Kafka, Actuator) |
| БД | PostgreSQL 16, миграции Flyway, Hibernate 7 (`ddl-auto: validate`) |
| Кэш | Redis 7 |
| Брокер | Apache Kafka 3.8 |
| Тесты | JUnit 5, Mockito, AssertJ, MockMvc, Testcontainers |
| Сборка | Maven (wrapper в репозитории) |

## Структура

```
src/main/java/com/shopflow/orders
├── catalog        товары, кэш цен в Redis
├── inventory      остатки и резервирование
├── cart           корзина покупателя
├── pricing        промокоды, расчёт скидки, распределение скидки по позициям (54-ФЗ)
├── order          заказ и его жизненный цикл
├── payment        оплата: card / sbp / crypto, callback от провайдеров
├── checkout       оформление заказа (оркестрация всех модулей выше)
├── notification   публикация событий заказа в Kafka
└── common         ошибки, конфигурация, общие классы

docs/
├── ONBOARDING.md       вводная для нового разработчика  ← начни отсюда
├── ARCHITECTURE.md     архитектура, правила зависимостей между модулями, потоки
├── SPECIFICATION.md    бизнес-требования заказчика (ТЗ)
├── api/openapi.yaml    REST-контракт
├── api/asyncapi.yaml   контракт событий в Kafka
├── adr/                архитектурные решения
└── tickets/            бэклог: баги и техдолг из Jira
http/requests.http      готовые запросы для IntelliJ HTTP Client
```

## Как запустить

### Вариант 1: инфраструктура в Docker Compose

```bash
cp .env.example .env
docker compose up -d          # postgres, redis, kafka
./mvnw spring-boot:run
```

### Вариант 2: всё через Testcontainers

Нужен только запущенный Docker. Запусти `TestOrderServiceApplication` из `src/test/java`:
он сам поднимет Postgres, Redis и Kafka в контейнерах.

### Проверка

```bash
curl localhost:8080/actuator/health
curl localhost:8080/api/v1/products
```

Сценарии «добавить в корзину → применить промокод → оформить заказ → callback от СБП» лежат
в [http/requests.http](http/requests.http).

## Тесты

```bash
./mvnw test       # юнит-тесты и slice-тесты (@WebMvcTest)
./mvnw verify     # + интеграционные тесты (*IT) на Testcontainers, нужен Docker
```

## Тестовые данные

Миграции создают демо-каталог (товары `1001`–`1005`) и промокоды:

| Код | Скидка | Условие |
|---|---|---|
| `SALE15` | 15 % | — |
| `MINUS500` | 500 ₽ | заказ от 3000 ₽ |
| `WELCOME1000` | 1000 ₽ | — |

Песочница эквайринга управляется токеном: `tok_ok` — успешная оплата, `tok_declined` — отказ банка,
`tok_error` — банк недоступен.

## Команда

| Роль | Кто |
|---|---|
| Tech Lead | Андрей К. |
| Backend | Василий П. (оплата: СБП), Пётр С. (оплата: крипто), **ты** |
| QA | Мария Л. |
| PM со стороны заказчика | Ольга Н. |

Процесс работы описан в [CONTRIBUTING.md](CONTRIBUTING.md).
