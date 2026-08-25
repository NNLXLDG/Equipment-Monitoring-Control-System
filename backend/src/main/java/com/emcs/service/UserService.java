package com.emcs.service;

import com.emcs.common.BusinessException;
import com.emcs.common.PageResult;
import com.emcs.dto.UserSaveDTO;
import com.emcs.dto.UserVO;
import com.emcs.entity.Role;
import com.emcs.entity.User;
import com.emcs.repository.RoleRepository;
import com.emcs.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public PageResult<UserVO> page(String keyword, int page, int size) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("username"), like),
                        cb.like(root.get("realName"), like),
                        cb.like(root.get("phone"), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<User> result = userRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "id")));
        List<UserVO> vos = result.getContent().stream().map(this::toVO).toList();
        return PageResult.of(vos, result.getTotalElements(), page, size);
    }

    public UserVO get(Long id) {
        return toVO(userRepository.findById(id).orElseThrow(() -> new BusinessException("用户不存在")));
    }

    @Transactional
    public void create(UserSaveDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException("用户名和密码不能为空");
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername().trim());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        applyCommon(user, dto);
        userRepository.save(user);
    }

    @Transactional
    public void update(Long id, UserSaveDTO dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new BusinessException("用户不存在"));
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        applyCommon(user, dto);
        userRepository.save(user);
    }

    private void applyCommon(User user, UserSaveDTO dto) {
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        user.setRemark(dto.getRemark());
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(dto.getRoleIds()));
            user.setRoles(roles);
        } else {
            user.setRoles(new HashSet<>());
        }
    }

    @Transactional
    public void delete(Long id) {
        if (id == 1L) {
            throw new BusinessException("内置管理员不可删除");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        User user = userRepository.findById(id).orElseThrow(() -> new BusinessException("用户不存在"));
        if (id == 1L && status == 0) {
            throw new BusinessException("内置管理员不可停用");
        }
        user.setStatus(status);
        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(Long id, String password) {
        if (!StringUtils.hasText(password)) {
            throw new BusinessException("密码不能为空");
        }
        User user = userRepository.findById(id).orElseThrow(() -> new BusinessException("用户不存在"));
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setRemark(user.getRemark());
        vo.setCreateTime(user.getCreateTime());
        if (user.getRoles() != null) {
            for (Role r : user.getRoles()) {
                vo.getRoleIds().add(r.getId());
                vo.getRoleNames().add(r.getRoleName());
            }
        }
        return vo;
    }
}
