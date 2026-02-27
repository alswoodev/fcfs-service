package com.fcfs.couponcore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fcfs.couponcore.entity.Coupon;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {
    
}