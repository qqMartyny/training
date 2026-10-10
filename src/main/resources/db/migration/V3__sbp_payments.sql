-- SHOP-212: оплата через СБП. Оплата асинхронная, заказ ждёт подтверждения в статусе AWAITING_PAYMENT.
alter table payments
    add column sbp_qr_payload varchar(512);

alter table orders
    drop constraint orders_status_check;

alter table orders
    add constraint orders_status_check
        check (status in ('NEW', 'AWAITING_PAYMENT', 'PAID', 'PAYMENT_FAILED', 'SHIPPED', 'DELIVERED', 'CANCELLED'));
