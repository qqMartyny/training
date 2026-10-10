package com.shopflow.orders.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockRepository extends JpaRepository<StockItem, Long> {

	/**
	 * Атомарно списывает остаток. Возвращает число изменённых строк: 0 — товара не хватает.
	 */
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update StockItem s set s.available = s.available - :quantity, s.version = s.version + 1 "
			+ "where s.productId = :productId and s.available >= :quantity")
	int tryReserve(@Param("productId") Long productId, @Param("quantity") int quantity);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update StockItem s set s.available = s.available + :quantity, s.version = s.version + 1 "
			+ "where s.productId = :productId")
	int release(@Param("productId") Long productId, @Param("quantity") int quantity);

}
