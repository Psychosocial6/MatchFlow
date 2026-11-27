package ru.matchflow.security;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import ru.matchflow.entities.UserRole;

import java.io.Serial;

@Getter
public final class AccountUserDetails extends User {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int userId;
    private final UserRole role;

    @Builder(builderMethodName = "accountBuilder")
    private AccountUserDetails(int userId, String username, String passwordHash, UserRole role) {
        super(username, passwordHash, AuthorityUtils.createAuthorityList("ROLE_" + role.name()));
        this.userId = userId;
        this.role = role;
    }

}
