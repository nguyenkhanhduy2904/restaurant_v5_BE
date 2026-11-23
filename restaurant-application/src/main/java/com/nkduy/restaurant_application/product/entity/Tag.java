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
import java.util.List;

@Builder
@Getter
@Setter
@Entity
@Table(name = "tag")
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String id;


    @NotNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;


    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private TagStatus status = TagStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;






}