package com.example.haksikmokjang.global.security;

import com.example.haksikmokjang.member.core.domain.AccountStatus;
import com.example.haksikmokjang.member.core.domain.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Member member;

    public CustomUserDetails(Member member) {
        this.member = member;
    }

    public Long getMemberId() {
        return member.getMemberId();
    }

    public String getRole() {
        return member.getRole().name();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));
    }

    @Override
    public String getPassword() {
        return member.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return member.getLoginId();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !member.isLocked();
    }

    @Override
    public boolean isEnabled() {
        return member.getAccountStatus() == AccountStatus.ACTIVE;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}