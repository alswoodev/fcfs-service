package com.fcfs.couponcore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fcfs.couponcore.entity.CouponIssue;

public interface CouponIssueJpaRepository extends JpaRepository<CouponIssue, Long> {
}