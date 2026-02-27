package com.fcfs.couponcore.entity;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fcfs.couponcore.exception.CouponIssueException;
import com.fcfs.couponcore.exception.ErrorCode;

public class CouponTest {
    @Test
    @DisplayName("If quantity issued is less than total quantity, it should be available for issue")   
    public void testIsAvailableForIssue_FirstComeFirstServed() {
        //given
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .totalQuantity(100)
                .issuedQuantity(50)
                .build();
        //when
        boolean result = coupon.isAvailableForIssue();

        //then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("If quantity issued is equal to total quantity, it should not be available for issue")   
    public void testIsAvailableForIssue_FirstComeFirstServed_LimitReached() {
        //given
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .totalQuantity(100)
                .issuedQuantity(100)
                .build();
        //when
        boolean result = coupon.isAvailableForIssue();

        //then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("If total quantity is null, it should be available for issue regardless of issued quantity")   
    public void testIsAvailableForIssue_FirstComeFirstServed_NoLimit() {
        //given
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .totalQuantity(null)
                .issuedQuantity(1000)
                .build();
        //when
        boolean result = coupon.isAvailableForIssue();

        //then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("If issued time is before issue start time, it should not be available for issue")
    public void testIsAvailableForIssue_BeforeStartTime() {
        //given
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .totalQuantity(100)
                .issuedQuantity(50)
                .dateIssueStart(LocalDateTime.now().plusDays(1)) // Future start time
                .build();
        //when
        boolean result = coupon.isWithinIssuePeriod();

        //then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("If issued time is within issue period and quantity is not exceeded, it should be available for issue")
    public void testIsAvailableForIssue_WithinIssuePeriod() {
        //given
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .totalQuantity(100)
                .issuedQuantity(50)
                .dateIssueStart(LocalDateTime.now().minusDays(1)) // Past start time
                .dateIssueEnd(LocalDateTime.now().plusDays(1)) // Future end time
                .build();
        //when
        boolean result = coupon.isWithinIssuePeriod();

        //then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("If issued time is after issue end time, it should not be available for issue")
    public void testIsAvailableForIssue_AfterEndTime() {
        //given
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .totalQuantity(100)
                .issuedQuantity(50)
                .dateIssueStart(LocalDateTime.now().minusDays(5)) // Past start time
                .dateIssueEnd(LocalDateTime.now().minusDays(1)) // Past end time
                .build();
        //when
        boolean result = coupon.isWithinIssuePeriod();

        //then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("If issued time is within issue period but quantity is exceeded, throw exception")
    public void testIssue_WithinIssuePeriod_ExceedQuantity() {
        //given
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(1)) // Past start time
                .dateIssueEnd(LocalDateTime.now().plusDays(1)) // Future end time
                .build();

        //when
        CouponIssueException e = Assertions.assertThrows(CouponIssueException.class, coupon::issue);

        //then
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    @DisplayName("If quantity is not exceeded but issued time is not within issue period, throw exception")
    public void testIssue_ProperQuantity_NotWithinIssuePeriod() {
        //given
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .totalQuantity(100)
                .issuedQuantity(50)
                .dateIssueStart(LocalDateTime.now().minusDays(5)) // Past start time
                .dateIssueEnd(LocalDateTime.now().minusDays(3)) // Future end time
                .build();

        //when
        CouponIssueException e = Assertions.assertThrows(CouponIssueException.class, coupon::issue);

        //then
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_COUPON_ISSUE_TIME);
    }

}