package floorida.example.floorida.Item.UserDetails;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String email;
    private final String password;

    public CustomUserDetails(Long userId, String email, String password) {
        this.userId = userId;
        this.email = email;
        this.password = password;
    }

    // ⭐ 우리가 쓰고 싶은 핵심 값
    public Long getUserId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한 아직 없으면 비워도 됨
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        // username 대신 email 쓰는 구조라면 email 반환
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}