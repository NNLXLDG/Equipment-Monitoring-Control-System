package com.emcs.service;

import com.emcs.common.BusinessException;
import com.emcs.common.PageResult;
import com.emcs.entity.Permission;
import com.emcs.entity.Role;
import com.emcs.repository.PermissionRepository;
import com.emcs.repository.RoleRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public List<Role> listAll() {
        return roleRepository.findAllByOrderByIdAsc();
    }

    public PageResult<Role> page(String keyword, int page, int size) {
        Specification<Role> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("roleName"), like),
                        cb.like(root.get("roleCode"), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Role> result = roleRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "id")));
        result.getContent().forEach(r -> r.setPermissions(null));
        return PageResult.of(result);
    }

    public Role get(Long id) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new BusinessException("角色不存在"));
        role.setPermissions(null);
        return role;
    }

    @Transactional
    public void create(Role role) {
        if (!StringUtils.hasText(role.getRoleCode())) {
            throw new BusinessException("角色编码不能为空");
        }
        if (roleRepository.existsByRoleCode(role.getRoleCode())) {
            throw new BusinessException("角色编码已存在");
        }
        if (role.getStatus() == null) role.setStatus(1);
        role.setId(null);
        role.setPermissions(new HashSet<>());
        roleRepository.save(role);
    }

    @Transactional
    public void update(Long id, Role role) {
        Role r = roleRepository.findById(id).orElseThrow(() -> new BusinessException("角色不存在"));
        r.setRoleName(role.getRoleName());
        r.setDescription(role.getDescription());
        r.setStatus(role.getStatus() == null ? 1 : role.getStatus());
        roleRepository.save(r);
    }

    @Transactional
    public void delete(Long id) {
        if (id <= 3L) {
            throw new BusinessException("内置角色不可删除");
        }
        roleRepository.deleteById(id);
    }

    public List<Long> getPermissionIds(Long id) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new BusinessException("角色不存在"));
        return role.getPermissions().stream().map(Permission::getId).toList();
    }

    @Transactional
    public void assignPermissions(Long id, List<Long> permissionIds) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new BusinessException("角色不存在"));
        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(
                permissionIds == null ? new ArrayList<>() : permissionIds));
        role.setPermissions(permissions);
        roleRepository.save(role);
    }
}
