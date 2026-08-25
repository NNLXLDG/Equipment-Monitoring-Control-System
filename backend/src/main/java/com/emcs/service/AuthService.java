package com.emcs.service;

import com.emcs.common.BusinessException;
import com.emcs.dto.UserInfoVO;
import com.emcs.entity.Permission;
import com.emcs.entity.Role;
import com.emcs.entity.User;
import com.emcs.repository.UserRepository;
import com.emcs.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;

    public Map<String, Object> login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(400, "用户名或密码错误"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(400, "账号已被停用");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", buildUserInfo(user));
        return result;
    }

    public UserInfoVO currentUser() {
        User user = com.emcs.security.SecurityUtils.getCurrentUser();
        return buildUserInfo(user);
    }

    public UserInfoVO buildUserInfo(User user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());

        Set<Permission> allPerms = new LinkedHashSet<>();
        Set<String> roleCodes = new LinkedHashSet<>();
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                roleCodes.add(role.getRoleCode());
                if (role.getPermissions() != null) {
                    allPerms.addAll(role.getPermissions());
                }
            }
        }
        vo.setRoles(new ArrayList<>(roleCodes));

        List<String> permCodes = allPerms.stream()
                .map(Permission::getPerm)
                .filter(Objects::nonNull)
                .filter(p -> !p.isBlank())
                .distinct()
                .sorted()
                .toList();
        vo.setPermissions(permCodes);
        vo.setMenus(permissionService.buildMenuTree(allPerms));
        return vo;
    }
}
