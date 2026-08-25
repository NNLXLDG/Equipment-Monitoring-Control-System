package com.emcs.service;

import com.emcs.common.BusinessException;
import com.emcs.dto.MenuVO;
import com.emcs.entity.Permission;
import com.emcs.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public List<Permission> listAll() {
        return permissionRepository.findAllByOrderBySortAsc();
    }

    /**
     * 全量权限树
     */
    public List<MenuVO> tree() {
        return buildTree(listAll());
    }

    /**
     * 根据用户拥有的权限构建菜单树（仅目录/菜单，不含按钮）
     */
    public List<MenuVO> buildMenuTree(Collection<Permission> permissions) {
        List<Permission> menus = permissions.stream()
                .filter(p -> p.getType() != null && p.getType() != 3)
                .sorted(Comparator.comparing(p -> p.getSort() == null ? 0 : p.getSort()))
                .collect(Collectors.toList());
        return buildTree(menus);
    }

    private List<MenuVO> buildTree(List<Permission> permissions) {
        Map<Long, MenuVO> map = new LinkedHashMap<>();
        for (Permission p : permissions) {
            MenuVO vo = new MenuVO();
            vo.setId(p.getId());
            vo.setParentId(p.getParentId());
            vo.setName(p.getName());
            vo.setType(p.getType());
            vo.setPath(p.getPath());
            vo.setComponent(p.getComponent());
            vo.setIcon(p.getIcon());
            map.put(p.getId(), vo);
        }
        List<MenuVO> roots = new ArrayList<>();
        for (MenuVO vo : map.values()) {
            Long parentId = vo.getParentId();
            if (parentId == null || parentId == 0L || !map.containsKey(parentId)) {
                roots.add(vo);
            } else {
                map.get(parentId).getChildren().add(vo);
            }
        }
        return roots;
    }

    public Permission create(Permission permission) {
        if (permission.getParentId() == null) permission.setParentId(0L);
        if (permission.getSort() == null) permission.setSort(0);
        if (permission.getStatus() == null) permission.setStatus(1);
        return permissionRepository.save(permission);
    }

    public Permission update(Long id, Permission permission) {
        Permission p = permissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("权限不存在"));
        p.setParentId(permission.getParentId());
        p.setName(permission.getName());
        p.setType(permission.getType());
        p.setPath(permission.getPath());
        p.setComponent(permission.getComponent());
        p.setPerm(permission.getPerm());
        p.setIcon(permission.getIcon());
        p.setSort(permission.getSort());
        p.setStatus(permission.getStatus());
        return permissionRepository.save(p);
    }

    public void delete(Long id) {
        if (permissionRepository.existsByParentId(id)) {
            throw new BusinessException("存在子节点，无法删除");
        }
        permissionRepository.deleteById(id);
    }
}
