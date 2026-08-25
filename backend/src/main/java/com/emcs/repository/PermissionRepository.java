package com.emcs.repository;

import com.emcs.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    List<Permission> findAllByOrderBySortAsc();
    List<Permission> findByParentIdOrderBySortAsc(Long parentId);
    boolean existsByParentId(Long parentId);
}
