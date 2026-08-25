package com.emcs.security;

import com.emcs.entity.Permission;
import com.emcs.entity.Role;
import com.emcs.entity.User;
import com.emcs.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                if (jwtUtil.validate(token)) {
                    String username = jwtUtil.getUsername(token);
                    userRepository.findByUsername(username).ifPresent(user -> {
                        if (user.getStatus() != null && user.getStatus() == 1) {
                            var auth = new UsernamePasswordAuthenticationToken(
                                    user, null, buildAuthorities(user));
                            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    });
                }
            } catch (Exception ignored) {
                // token 无效则保持匿名
            }
        }
        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> buildAuthorities(User user) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()));
                if (role.getPermissions() != null) {
                    for (Permission p : role.getPermissions()) {
                        if (StringUtils.hasText(p.getPerm())) {
                            authorities.add(new SimpleGrantedAuthority(p.getPerm()));
                        }
                    }
                }
            }
        }
        return authorities;
    }
}
