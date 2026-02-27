package com.fcfs.couponcore.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fcfs.couponcore.TestConfig;
import com.fcfs.couponcore.entity.Coupon;
import com.fcfs.couponcore.entity.CouponIssue;
import com.fcfs.couponcore.entity.CouponType;
import com.fcfs.couponcore.exception.CouponIssueException;
import com.fcfs.couponcore.exception.ErrorCode;
import com.fcfs.couponcore.repository.CouponIssueRepository;
import com.fcfs.couponcore.repository.CouponJpaRepository;

public class CouponIssueServiceTest extends TestConfig {

    @Autowired
    private CouponIssueRepository couponIssueRepository;

    @Autowired
    private CouponJpaRepository couponJpaRepository;

    @Autowired
    private CouponIssueService couponIssueService;

    private Coupon coupon;
    private Long userId = 100L;

    @BeforeEach
    void setUp() {
        coupon = Coupon.builder()
                .title("Test Coupon")
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .totalQuantity(100)
                .issuedQuantity(10)
                .discountAmount(5000)
                .minAvailableAmount(10000)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        coupon = couponJpaRepository.save(coupon);
    }

    @DisplayName("Should throw DUPLICATE_COUPON_ISSUE exception when coupon issue already exists (Alternative)")
    @Test
    void shouldThrowDuplicateCouponIssueException_WhenCouponIssueAlreadyExists_Alternative() {
        // given
        couponIssueService.issueCoupon(coupon.getId(), userId);

        // when & then
        assertThatThrownBy(() -> couponIssueService.issueCoupon(coupon.getId(), userId))
                    .isInstanceOf(CouponIssueException.class)
                    .extracting(ex -> ((CouponIssueException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.DUPLICATE_COUPON_ISSUE);
    }

    @DisplayName("Should issue coupon successfully when coupon exists and no previous issue")
    @Test
    void shouldIssueCouponSuccessfully_WithValidCouponAndNoExistingIssue() {
        // given
        Long testUserId = 300L;

        // when
        couponIssueService.issueCoupon(coupon.getId(), testUserId);

        // then
        CouponIssue issuedCoupon = couponIssueRepository.findFirstCouponIssue(coupon.getId(), testUserId);
        assertThat(issuedCoupon).isNotNull();
        assertThat(issuedCoupon.getCoupon().getId()).isEqualTo(coupon.getId());
        assertThat(issuedCoupon.getUserId()).isEqualTo(testUserId);
    }

    @DisplayName("Should throw COUPON_NOT_EXISTS exception when coupon does not exist")
    @Test
    void shouldThrowCouponNotExistsException_WhenCouponDoesNotExist() {
        // given
        long nonExistentCouponId = 999L;

        // when & then
        assertThatThrownBy(
                () -> couponIssueService.issueCoupon(nonExistentCouponId, userId)
        ).isInstanceOf(CouponIssueException.class)
        .as("Should throw CouponIssueException")
        .extracting(ex -> ((CouponIssueException) ex).getErrorCode())
        .isEqualTo(ErrorCode.COUPON_NOT_EXISTS);
    }
}
