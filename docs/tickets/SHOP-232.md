# SHOP-232: На CI падает contextLoads, локально у всех зелёное

| | |
|---|---|
| Тип | Bug |
| Приоритет | **Blocker** |
| Компоненты | tests, ci |
| Автор | Игорь Д. (DevOps) |

## Описание

После исправления компиляции пайплайн всё равно красный:

```
[ERROR] Tests run: 1, Failures: 0, Errors: 1 -- in com.shopflow.orders.OrderServiceApplicationTests
Caused by: org.postgresql.util.PSQLException: Connection to localhost:5432 refused.
```

У разработчиков локально тест зелёный. На CI-раннере Docker есть, Testcontainers работают
(соседние проекты на нём собираются).

## Комментарии

**Мария Л. (QA):** У меня тоже зелёный, но у меня всегда запущен `docker compose` от этого проекта.
Если его остановить, тест падает так же, как на CI.

## Критерии приёмки

- [ ] Тест проходит на машине, где установлен только Docker, а `docker compose` не запущен.
- [ ] Ни один тест не зависит от локально поднятой инфраструктуры.
