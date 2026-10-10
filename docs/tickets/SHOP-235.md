# SHOP-235: Оформление заказа падает с 500 после включения уведомлений

| | |
|---|---|
| Тип | Bug |
| Приоритет | **Critical** |
| Компоненты | notification, checkout |
| Автор | Мария Л. (QA) |
| Связано | SHOP-201, [ADR-0003](../adr/0003-order-events.md), [asyncapi.yaml](../api/asyncapi.yaml) |

## Описание

Любой `POST /api/v1/checkout` возвращает 500. В логе:

```
ERROR c.s.o.c.e.GlobalExceptionHandler : Unexpected error
org.apache.kafka.common.errors.SerializationException: Can't convert value of class
    com.shopflow.orders.notification.OrderEventMessage to class ...
```

При этом заказ в БД не появляется. Команда Notification Service говорит, что ждёт JSON по контракту
из `asyncapi.yaml`.

## Критерии приёмки

- [ ] Checkout проходит.
- [ ] Сообщение в топике — JSON, соответствующий `asyncapi.yaml`. Проверь это тестом, который
      **читает сообщение из Kafka** и разбирает его, а не только смотрит, что «что-то пришло».
- [ ] Подумай: правильно ли, что недоступность или ошибка Kafka ломает оформление заказа?
      Ответ запиши в описание PR (подсказка: ADR-0003).
