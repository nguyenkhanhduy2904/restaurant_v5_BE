package com.nkduy.restaurant_application.voucher.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
@Builder
@Getter
@Setter
@Entity
@Table(name = "delivery_voucher")
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryVoucher {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 50)
    @NotNull
    @Column(name = "discount_type", nullable = false, length = 50)
    private String discountType;

    @NotNull
    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Size(max = 255)
    @Column(name = "code")
    private String code;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @NotNull
    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @NotNull
    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Column(name = "max_claim", columnDefinition = "int UNSIGNED not null")
    private Long maxClaim;

    @Builder.Default
    @Column(name = "current_claim", columnDefinition = "int UNSIGNED not null")
    private Long currentClaim = 0L;

    @Column(name = "min_delivery_fee", precision = 12)
    private BigDecimal minDeliveryFee;

    @Column(name = "max_discount", precision = 12)
    private BigDecimal maxDiscount;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 50)
    private String status= "ACTIVE";


}