package ru.matchflow.services;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.matchflow.entities.UserAccount;
import ru.matchflow.repositories.UserRepository;
import ru.matchflow.security.AccountUserDetails;

import java.util.regex.Pattern;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private static final Pattern BCRYPT_HASH = Pattern.compile("\\$2[aby]\\$(0[4-9]|[12][0-9]|3[01])\\$[./A-Za-z0-9]{53}");

    private final UserRepository users;

    public DatabaseUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public AccountUserDetails loadUserByUsername(String username) {
        UserAccount account = users.findByUsername(username)
                .filter(user -> user.getPasswordHash() != null && BCRYPT_HASH.matcher(user.getPasswordHash()).matches())
                .orElseThrow(() -> new UsernameNotFoundException("No password account found"));

        return AccountUserDetails.accountBuilder()
                .userId(account.getId())
                .username(account.getUsername())
                .passwordHash(account.getPasswordHash())
                .role(account.getRole())
                .build();
    }
}
