# Эталонное решение (спойлер!)

Ветка `solution` содержит одну из возможных реализаций, в которой закрыты все тикеты бэклога.
Сравнивай с ней только после того, как сам закроешь тикет или этап. Твоё решение может отличаться
и при этом быть верным, если выполняет критерии приёмки.

| Тикет | Корневая причина | Решение в эталоне |
|---|---|---|
| 231 | `switch` по `PaymentMethod` без `CRYPTO` | Сбор перенесён в стратегию: `PaymentProcessor.fee()`, `PaymentFeeCalculator` удалён |
| 231 | Два файла миграции `V3__` из параллельных веток | Перенумерация в `V3__sbp`, `V4__crypto` |
| 231 | Колонка `wallet_address` вместо `crypto_wallet_address` | Миграция приведена к маппингу сущности |
| 231 | Цикл `CartService` ↔ `PricingService` | `pricing` не знает о корзине: `CartService.quote()` сам готовит позиции |
| 231 | Один `PaymentProcessor` на три бина | Strategy + `PaymentProcessorRegistry` (`List` → `EnumMap`, fail-fast на дубликатах) |
| 231 | Нет `api-key` крипты в `application.yml` | `${CRYPTO_API_KEY:sandbox-key}` |
| 232 | `contextLoads` без `@Import(TestcontainersConfiguration)` | Добавлен импорт; общий `AbstractIntegrationTest` |
| 233 | Нет `maven-failsafe-plugin`, поэтому `*IT` не запускаются | Плагин добавлен в `pom.xml` |
| 234 | В Redis кладётся JPA-сущность | Кэшируется `ProductDto` (`record`, `Serializable`) |
| 235 | `StringSerializer` для значения | `JacksonJsonSerializer`, без type headers |
| 236 | `PENDING` трактуется как неуспех; check-constraint без `AWAITING_PAYMENT` | `switch` по `PaymentStatus`; constraint пересоздаётся в миграции V3 |
| 237 | `setScale(2)` для BTC, нет сбора | Точность 8 знаков, `RoundingMode.UP`; сбор 1,5 % в стратегии |
| 238 | Строковый тип `"PERCENT"` против `PERCENTAGE` в БД | `enum DiscountType` + стратегии `DiscountPolicy` |
| 239 | Скидка не ограничена суммой | `discount.min(subtotal)` |
| 240 | Деление скидки поровну без округления | Пропорциональное распределение, остаток на последнюю позицию |
| 241 | `Long == Long` (кэш `Long` только для −128…127) | `Objects.equals` |
| 242 | Контроллер отдаёт сущность с двунаправленной связью | Возвращается `CartView` |
| 243 | Нет `@Valid` на `@RequestBody` | Добавлен, плюс негативный тест |
| 244 | Ленивая коллекция читается вне транзакции; тест маскирует это `@Transactional` | Маппинг в DTO внутри `@Transactional(readOnly)`, `@EntityGraph`; тест без `@Transactional` |
| 245 | `HashSet<OrderItem>` + `equals/hashCode` по `id == null` | `List<OrderItem>` без переопределения `equals` |
| 246 | Поиск заказа только по `id` | `findWithItemsByIdAndCustomerId`, 404 для чужого заказа |
| 247 | Публичный `setStatus` без правил | Таблица переходов в `OrderStatus`, `Order.changeStatus()` бросает 409 |
| 248 | Callback не идемпотентен | Повтор по завершённому платежу игнорируется; `PENDING` игнорируется; `@Version` |
| 249 | Схема «прочитать → проверить → записать» | Атомарный `UPDATE ... WHERE available >= :qty` |
| 250 | `@Transactional` на self-invocation; нет компенсации | Резерв внутри транзакционного `OrderService.placeOrder` (`MANDATORY`); `release` при `CANCELLED`/`PAYMENT_FAILED` |
| 251 | `kafkaTemplate.send` внутри транзакции из домена | Доменное событие + `@TransactionalEventListener(AFTER_COMMIT)` в `notification` |
| 252 | Сообщение без ключа | Ключ = `orderId` |
| 253 | Нет инвалидации кэша | `@CacheEvict` на изменении цены |
| 254 | `NotFoundException` не обработан | Отдельный `@ExceptionHandler` → 404 |
| 255 | `record.toString()` с токеном, `show-sql: true` | `toString()` маскирует токен; `show-sql` выключен |
| 256 | Фейковые тесты | Переписаны: см. тесты в ветке `solution` |
