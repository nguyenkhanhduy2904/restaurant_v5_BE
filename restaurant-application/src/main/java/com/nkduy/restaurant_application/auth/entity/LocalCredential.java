package com.nkduy.restaurant_application.auth.entity;

import com.nkduy.restaurant_application.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
@Builder
@Getter
@Setter
@Entity
@Table(name = "local_credential")
@NoArgsConstructor
@AllArgsConstructor
public class LocalCredential {
    @Id
    @NonNull
    @Column(name = "email", nullable = false)
    private String email;

    @NotNull
    @Column(name = "password_hashed", nullable = false)
    private String passwordHashed;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;


}