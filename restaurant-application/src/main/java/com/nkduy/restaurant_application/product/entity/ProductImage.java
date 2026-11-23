package com.nkduy.restaurant_application.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
@Builder
@Getter
@Setter
@Entity
@Table(name = "product_image")
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String id;

//    @Size(max = 255)
//    @NotNull
//    @Column(name = "url", nullable = false)
//    private String url;

    @Column(name = "display_order", columnDefinition = "int UNSIGNED not null")
    private Long displayOrder;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Size(max = 255)
    @NotNull
    @Column(name = "object_key", nullable = false)
    private String objectKey;


}