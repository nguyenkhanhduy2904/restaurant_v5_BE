package com.nkduy.restaurant_application.order.entity;

import com.nkduy.restaurant_application.branch.entity.Branch;
import com.nkduy.restaurant_application.user.entity.User;
import com.nkduy.restaurant_application.voucher.entity.UserDeliveryVoucher;
import com.nkduy.restaurant_application.voucher.entity.UserOrderVoucher;
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
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String id;


    @Column(name = "price_before_discount", nullable = false, precision = 12)
    private BigDecimal priceBeforeDiscount;


    @Column(name = "total_item", nullable = false)
    private Integer totalItem;


    @Builder.Default
    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING";

    @Size(max = 255)
    @Column(name = "address")
    private String address;

    @NotNull
    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @NotNull
    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Size(max = 20)
    @NotNull
    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;

    @Size(max = 50)
    @NotNull
    @Column(name = "contact_name", nullable = false, length = 50)
    private String contactName;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "delivery_fee", nullable = false, precision = 12)
    private BigDecimal deliveryFee;

    @NotNull
    @Column(name = "delivery_distance", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryDistance;

    @NotNull
    @Column(name = "order_discounted", nullable = false, precision = 12)
    private BigDecimal orderDiscounted;

    @NotNull
    @Column(name = "delivery_discount", nullable = false, precision = 12)
    private BigDecimal deliveryDiscount;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_order_voucher_id")
    private UserOrderVoucher userOrderVoucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_delivery_voucher_id")
    private UserDeliveryVoucher userDeliveryVoucher;

    @NotNull
    @Column(name = "final_price", nullable = false, precision = 12)
    private BigDecimal finalPrice;


}