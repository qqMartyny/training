-- SHOP-215: оплата криптовалютой.
alter table payments
    add column wallet_address varchar(128),
    add column crypto_amount  numeric(20, 8);
