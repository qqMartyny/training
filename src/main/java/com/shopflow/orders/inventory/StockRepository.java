package com.shopflow.orders.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<StockItem, Long> {

}
