package be.uliege.speam.team03.MDTools.services;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.*;
import org.springframework.security.oauth2.core.oidc.user.*;
import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.mapper.UserMapper;
import be.uliege.speam.team03.MDTools.models.User;
import be.uliege.speam.team03.MDTools.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Service
@Slf4j
public class OAuth2UserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    @SneakyThrows
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);

        // Prefer email as stable identifier
        final String email = (oidcUser.getEmail() != null) ? oidcUser.getEmail()
                : (String) oidcUser.getClaims().get("email");

        // Prefer preferred_username (Keycloak) then name then email
        final String preferredUsername = (String) oidcUser.getClaims().get("preferred_username");
        final String fullName = (String) oidcUser.getClaims().getOrDefault("name", preferredUsername);
        final String username = (preferredUsername != null && !preferredUsername.isBlank())
                ? preferredUsername
                : (fullName != null && !fullName.isBlank() ? fullName : email);

        if (email == null || email.isBlank()) {
            log.error("OIDC login without email. Claims keys={}", oidcUser.getClaims().keySet());
            // You can throw here if your app requires email
            // throw new IllegalStateException("Email is missing in OIDC token");
        }

        // Extract roles from Keycloak token
        final Set<String> roleNames = extractRolesClaim(oidcUser.getClaims());
        final Set<String> allowed = Set.of("ROLE_USER", "ROLE_ADMIN", "ROLE_WEBMASTER");
        final List<String> normalizedRoles = normalizeRoles(roleNames);

        final List<String> filteredRoles = normalizedRoles.stream()
                .filter(allowed::contains)
                .toList();



        // Sync user in DB
        User dbUser = userRepository.findByEmail(email).orElseGet(User::new);
        dbUser.setEmail(email);
        dbUser.setUsername(username);
        dbUser.setEnabled(true);
    dbUser.setAuthorities(UserMapper.toAuthorities(filteredRoles));


        dbUser = userRepository.save(dbUser);

        // Use DB authorities (source of truth after sync)
        final Collection<? extends GrantedAuthority> authorities = dbUser.getAuthorities();
        final Set<GrantedAuthority> springAuthorities = authorities.stream()
                .map(a -> new SimpleGrantedAuthority(a.getAuthority()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // Choose a stable "name attribute key" for DefaultOidcUser
        final String nameAttributeKey = oidcUser.getClaims().containsKey("preferred_username")
                ? "preferred_username"
                : (oidcUser.getClaims().containsKey("email") ? "email" : "sub");

        return new DefaultOidcUser(
                springAuthorities,
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                nameAttributeKey
        );
    }

    private Set<String> extractRolesClaim(Map<String, Object> claims) {
        Set<String> roles = new LinkedHashSet<>();

        Object rolesObj = claims.get("roles");

        if (rolesObj instanceof Collection<?> rolesCol) {
            rolesCol.forEach(r -> roles.add(String.valueOf(r)));
            return roles;
        }

        // Sometimes Keycloak can send it as a single string or comma-separated
        if (rolesObj instanceof String rolesStr) {
            for (String r : rolesStr.split(",")) {
                String trimmed = r.trim();
                if (!trimmed.isEmpty()) roles.add(trimmed);
            }
        }

        return roles;
    }


    private List<String> normalizeRoles(Set<String> rawRoles) {
        // Convert Keycloak roles like "admin" or "ADMIN" or "ROLE_ADMIN" -> "ROLE_ADMIN"
        return rawRoles.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(r -> !r.isBlank())
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r.toUpperCase(Locale.ROOT))
                .distinct()
                .sorted()
                .toList();
    }
}
