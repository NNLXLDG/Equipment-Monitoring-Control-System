package com.emcs.config;

import com.emcs.entity.*;
import com.emcs.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * 首次启动时初始化权限、角色、用户、示例设备与点位。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final DevicePointRepository pointRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initPermissions();
        initDevices();
        log.info("数据初始化检查完成");
    }

    private void initPermissions() {
        if (roleRepository.count() > 0) {
            return;
        }

        // 1. 创建权限（目录/菜单/按钮）
        List<Permission> all = new ArrayList<>();
        Map<String, Permission> byCode = new LinkedHashMap<>();

        Permission dirMonitor = savePerm(all, byCode, 0L, "设备监测", 1, "/monitor", null, null, "Monitor", 1);
        Permission device = savePerm(all, byCode, dirMonitor.getId(), "设备管理", 2, "device", "device/index", null, "Cpu", 10);
        savePerm(all, byCode, device.getId(), "设备新增", 3, null, null, "device:add", null, 11);
        savePerm(all, byCode, device.getId(), "设备修改", 3, null, null, "device:edit", null, 12);
        savePerm(all, byCode, device.getId(), "设备删除", 3, null, null, "device:delete", null, 13);
        Permission point = savePerm(all, byCode, dirMonitor.getId(), "点位管理", 2, "point", "point/index", null, "SetUp", 20);
        savePerm(all, byCode, point.getId(), "点位新增", 3, null, null, "point:add", null, 21);
        savePerm(all, byCode, point.getId(), "点位修改", 3, null, null, "point:edit", null, 22);
        savePerm(all, byCode, point.getId(), "点位删除", 3, null, null, "point:delete", null, 23);
        Permission data = savePerm(all, byCode, dirMonitor.getId(), "数据查询", 2, "data", "data/index", null, "DataLine", 30);
        Permission chart = savePerm(all, byCode, dirMonitor.getId(), "图形分析", 2, "chart", "chart/index", null, "TrendCharts", 40);
        Permission control = savePerm(all, byCode, dirMonitor.getId(), "控制面板", 2, "control", "control/index", null, "SwitchButton", 50);
        savePerm(all, byCode, control.getId(), "控制下发", 3, null, null, "control:send", null, 51);
        Permission report = savePerm(all, byCode, dirMonitor.getId(), "统计报表", 2, "report", "report/index", null, "PieChart", 60);

        Permission dirSystem = savePerm(all, byCode, 0L, "系统管理", 1, "/system", null, null, "Setting", 2);
        Permission user = savePerm(all, byCode, dirSystem.getId(), "用户管理", 2, "user", "system/user/index", null, "User", 70);
        savePerm(all, byCode, user.getId(), "用户新增", 3, null, null, "user:add", null, 71);
        savePerm(all, byCode, user.getId(), "用户修改", 3, null, null, "user:edit", null, 72);
        savePerm(all, byCode, user.getId(), "用户删除", 3, null, null, "user:delete", null, 73);
        Permission role = savePerm(all, byCode, dirSystem.getId(), "角色管理", 2, "role", "system/role/index", null, "UserFilled", 80);
        savePerm(all, byCode, role.getId(), "角色新增", 3, null, null, "role:add", null, 81);
        savePerm(all, byCode, role.getId(), "角色修改", 3, null, null, "role:edit", null, 82);
        savePerm(all, byCode, role.getId(), "角色删除", 3, null, null, "role:delete", null, 83);
        savePerm(all, byCode, dirSystem.getId(), "菜单管理", 2, "permission", "system/permission/index", null, "Menu", 90);

        // 2. 创建角色
        Role admin = createRole("超级管理员", "ADMIN", "系统最高权限");
        Role operator = createRole("运维人员", "OPERATOR", "日常运维");
        Role viewer = createRole("访客", "VIEWER", "只读查看");

        // 3. 分配权限
        admin.setPermissions(new HashSet<>(all));

        Set<Permission> operatorPerms = new HashSet<>();
        operatorPerms.add(dirMonitor);
        operatorPerms.add(device);
        operatorPerms.add(point);
        operatorPerms.add(data);
        operatorPerms.add(chart);
        operatorPerms.add(control);
        operatorPerms.add(report);
        // 运维人员可管理设备/点位/控制，但无系统管理
        for (Permission p : all) {
            if (p.getPerm() != null && (p.getPerm().startsWith("device:")
                    || p.getPerm().startsWith("point:") || p.getPerm().startsWith("control:"))) {
                operatorPerms.add(p);
            }
        }
        operator.setPermissions(operatorPerms);

        Set<Permission> viewerPerms = new HashSet<>();
        viewerPerms.add(dirMonitor);
        viewerPerms.add(data);
        viewerPerms.add(chart);
        viewerPerms.add(report);
        viewer.setPermissions(viewerPerms);

        roleRepository.saveAll(List.of(admin, operator, viewer));

        // 4. 创建默认用户
        createUser("admin", "123456", "系统管理员", List.of(admin));
        createUser("operator", "123456", "运维工程师", List.of(operator));
        createUser("viewer", "123456", "访客", List.of(viewer));

        log.info("已初始化权限/角色/用户");
    }

    private Permission savePerm(List<Permission> all, Map<String, Permission> byCode,
                                Long parentId, String name, int type, String path, String component,
                                String perm, String icon, int sort) {
        Permission p = new Permission();
        p.setParentId(parentId);
        p.setName(name);
        p.setType(type);
        p.setPath(path);
        p.setComponent(component);
        p.setPerm(perm);
        p.setIcon(icon);
        p.setSort(sort);
        p.setStatus(1);
        p = permissionRepository.save(p);
        all.add(p);
        if (perm != null) byCode.put(perm, p);
        return p;
    }

    private Role createRole(String name, String code, String desc) {
        Role role = new Role();
        role.setRoleName(name);
        role.setRoleCode(code);
        role.setDescription(desc);
        role.setStatus(1);
        role.setPermissions(new HashSet<>());
        return role;
    }

    private void createUser(String username, String rawPassword, String realName, List<Role> roles) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRealName(realName);
        user.setStatus(1);
        user.setRoles(new HashSet<>(roles));
        userRepository.save(user);
    }

    private void initDevices() {
        if (deviceRepository.count() > 0) {
            return;
        }
        Device d1 = createDevice("492C230518046576", "1号电热水壶", "电热设备", "KT-1000", "某电器厂", "一号车间A区", 1);
        Device d2 = createDevice("492C230518046577", "2号电热水壶", "电热设备", "KT-1000", "某电器厂", "一号车间B区", 1);
        Device d3 = createDevice("492C230518046578", "3号电热水壶", "电热设备", "KT-1000", "某电器厂", "二号车间A区", 0);

        addPoint(d1, "AI1", "温度", 1, 1, "℃", 0, 100, "0", "温度采集值");
        addPoint(d1, "AI2", "水位", 1, 1, "mm", 0, 5000, "4889", "水位采集值");
        addPoint(d1, "AI3", "加热状态", 1, 2, null, 0, 1, "0", "0停止 1加热");
        addPoint(d1, "AI4", "功率", 1, 1, "W", 0, 2000, "20", "功率采集值");
        addPoint(d1, "DO1", "加热开关", 2, 2, null, 0, 1, "0", "0关 1开");
        addPoint(d1, "DO2", "保温开关", 2, 2, null, 0, 1, "0", "0关 1开");
        addPoint(d1, "DO3", "蜂鸣器", 2, 2, null, 0, 1, "0", "0关 1开");
        addPoint(d1, "DO4", "指示灯", 2, 2, null, 0, 1, "0", "0关 1开");

        addPoint(d2, "AI1", "温度", 1, 1, "℃", 0, 100, "0", null);
        addPoint(d2, "AI2", "水位", 1, 1, "mm", 0, 5000, "4889", null);
        addPoint(d2, "AI3", "加热状态", 1, 2, null, 0, 1, "0", null);
        addPoint(d2, "AI4", "功率", 1, 1, "W", 0, 2000, "20", null);
        addPoint(d2, "DO1", "加热开关", 2, 2, null, 0, 1, "0", null);
        addPoint(d2, "DO2", "保温开关", 2, 2, null, 0, 1, "0", null);
        addPoint(d2, "DO3", "蜂鸣器", 2, 2, null, 0, 1, "0", null);
        addPoint(d2, "DO4", "指示灯", 2, 2, null, 0, 1, "0", null);
        log.info("已初始化示例设备与点位");
    }

    private Device createDevice(String devId, String name, String type, String model,
                                String manufacturer, String location, int status) {
        Device d = new Device();
        d.setDevId(devId);
        d.setDeviceName(name);
        d.setDeviceType(type);
        d.setModel(model);
        d.setManufacturer(manufacturer);
        d.setLocation(location);
        d.setStatus(status);
        d.setInstallDate(LocalDate.of(2023, 6, 1));
        d.setDescription("示例设备：上报主题 /kettle/pub");
        return deviceRepository.save(d);
    }

    private void addPoint(Device device, String code, String name, int pointType, int dataType,
                          String unit, int min, int max, String def, String desc) {
        DevicePoint p = new DevicePoint();
        p.setDeviceId(device.getId());
        p.setPointCode(code);
        p.setPointName(name);
        p.setPointType(pointType);
        p.setDataType(dataType);
        p.setUnit(unit);
        p.setMinValue(BigDecimal.valueOf(min));
        p.setMaxValue(BigDecimal.valueOf(max));
        p.setDefaultValue(def);
        p.setDescription(desc);
        pointRepository.save(p);
    }
}
