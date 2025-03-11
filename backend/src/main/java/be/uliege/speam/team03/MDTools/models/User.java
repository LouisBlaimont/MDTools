package be.uliege.speam.team03.MDTools.models;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_authorities",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "authority")
    )
    private Set<Authority> authorities;

    @Column(name = "enabled", columnDefinition = "boolean default false")
    private boolean enabled;
    
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

    public Collection<GrantedAuthority> getAuthorities() {
        if (authorities == null || authorities.isEmpty()) {
            return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        }
        Collection<GrantedAuthority> authoritiesCollection = new ArrayList<>(authorities.size());
        for (Authority authority : authorities) {
            authoritiesCollection.add(new SimpleGrantedAuthority(authority.getAuthority()));
        }
        return authoritiesCollection;
    }

    public List<String> getRoles() {
        List<String> roles = new ArrayList<>();
        for (Authority authority : authorities) {
            roles.add(authority.getAuthority());
        }
        return roles;
    }
}
