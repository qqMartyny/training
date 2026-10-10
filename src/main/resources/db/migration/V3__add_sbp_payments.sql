-- SHOP-212: оплата через СБП (динамический QR-код).
alter table payments
    add column sbp_qr_payload varchar(512);
