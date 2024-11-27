package be.uliege.speam.team03.MDTools.models;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@EqualsAndHashCode
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Nullable
    @Column(name = "password_fingerprint")
    private String password;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt; // it's probably better to use Timestamp instead of LocalDateTime

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, updatable = true)
    private Timestamp updatedAt;
    
    @Nullable
    @Column(name = "role_name")
    private String roleName;

    @Nullable
    @Column(name = "job_position")
    private String jobPosition;

    @Nullable
    @Column(name = "workplace")
    private String workplace;

    @Nullable
    @Column(name = "reset_token", unique = true)
    private String resetToken;

    @Nullable
    @Column(name = "reset_token_expiration")
    private LocalDateTime resetTokenExpiration;

}
