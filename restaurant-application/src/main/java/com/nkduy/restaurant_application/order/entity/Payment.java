package com.nkduy.restaurant_application.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
@Builder
@Getter
@Setter
@Entity
@Table(name = "payment")
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String id;


    @NotNull
    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;


    @NotNull
    @Column(name = "payment_status", nullable = false, length = 50)
    private String paymentStatus;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;


    @Column(name = "amount", nullable = false, precision = 12)
    private BigDecimal amount;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;


    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Size(max = 255)
    @Column(name = "transaction_ref")
    private String transactionRef;


    @Builder.Default
    @Column(name = "refund_status", length = 50)
    private String refundStatus = "NOT_REQUIRED";


}