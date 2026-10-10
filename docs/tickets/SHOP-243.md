# SHOP-243: В корзину можно добавить −5 товаров

| | |
|---|---|
| Тип | Bug |
| Приоритет | Major |
| Компоненты | cart |
| Автор | Мария Л. (QA) |

## Описание

```
POST /api/v1/cart/items {"productId": 1001, "quantity": 3}   → 200, quantity 3
POST /api/v1/cart/items {"productId": 1001, "quantity": -5}  → ...
POST /api/v1/cart/items {"productId": 1002, "quantity": -5}  → 500
POST /api/v1/cart/items {"productId": 1002, "quantity": 1000} → 200
```

Аннотации валидации на `AddItemRequest` стоят. Тест контроллера зелёный.

## Критерии приёмки

- [ ] Невалидный запрос → `400 VALIDATION_ERROR` (формат — `ErrorResponse`), сервис не вызывается.
- [ ] Тест контроллера проверяет и негативные сценарии.
- [ ] Проверь, что валидация работает на **всех** эндпоинтах с телом запроса.
