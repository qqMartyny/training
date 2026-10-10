package com.shopflow.orders.pricing;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PromoCodeRepository extends JpaRepository<PromoCode, String> {

	Optional<PromoCode> findByCodeIgnoreCase(String code);

}
