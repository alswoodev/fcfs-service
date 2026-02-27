package com.fcfs.couponcore.entity;

import java.time.LocalDateTime;

import com.fcfs.couponcore.exception.CouponIssueException;
import com.fcfs.couponcore.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name="coupons")
public class Coupon extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CouponType couponType;

    private Integer totalQuantity;

    @Column(nullable = false)
    private int issuedQuantity;

    @Column(nullable = false)
    private int discountAmount;

    @Column(nullable = false)
    private int minAvailableAmount;

    @Column(nullable = false)
    private LocalDateTime dateIssueStart;

    @Column(nullable = false)
    private LocalDateTime dateIssueEnd;

    public boolean isAvailableForIssue() {
        if (totalQuantity == null) return true; // When totalQuantity is null, it means there is no limit on the number of coupons that can be issued.
        if (couponType == CouponType.FIRST_COME_FIRST_SERVED) return issuedQuantity < totalQuantity;
        return true;
    }

    public boolean isWithinIssuePeriod() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(dateIssueStart) && now.isBefore(dateIssueEnd);
    }

    public void issue(){
        if (!isAvailableForIssue()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, 
                "Exceeded the maximum number of coupons that can be issued. total:%s, issued:%s".formatted(totalQuantity, issuedQuantity));
        }
        if (!isWithinIssuePeriod()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_TIME, 
                "Current time is not within the coupon issue period. now:%s, start:%s, end:%s".formatted(LocalDateTime.now(), dateIssueStart, dateIssueEnd));
        }
        issuedQuantity++;
    }

}