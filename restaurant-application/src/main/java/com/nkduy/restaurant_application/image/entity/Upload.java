package com.nkduy.restaurant_application.image.entity;

import com.nkduy.restaurant_application.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
@Builder
@Getter
@Setter
@Entity
@Table(name = "upload")
@NoArgsConstructor
@AllArgsConstructor
public class Upload {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String id;

    @Size(max = 512)
    @NotNull
    @Column(name = "object_key", nullable = false, length = 512)
    private String objectKey;

    @Size(max = 50)
    @NotNull
    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @NotNull
    @Column(name = "filesize", nullable = false)
    private Long filesize;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private UploadStatus status = UploadStatus.PENDING;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @CurrentTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;


}