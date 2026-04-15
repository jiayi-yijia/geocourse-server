package com.bddk.geocourse.module.compat.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.framework.tenant.TenantContextHolder;
import com.bddk.geocourse.module.compat.model.FrontendUserMetadata;
import com.bddk.geocourse.module.compat.model.RosterStudentCreateRequest;
import com.bddk.geocourse.module.compat.model.RosterStudentView;
import com.bddk.geocourse.module.compat.model.RosterTeacherCreateRequest;
import com.bddk.geocourse.module.compat.model.RosterTeacherView;
import com.bddk.geocourse.module.identity.dal.dataobject.SysRoleDO;
import com.bddk.geocourse.module.identity.dal.dataobject.SysUserDO;
import com.bddk.geocourse.module.identity.dal.dataobject.SysUserRoleDO;
import com.bddk.geocourse.module.identity.dal.mapper.SysRoleMapper;
import com.bddk.geocourse.module.identity.dal.mapper.SysUserMapper;
import com.bddk.geocourse.module.identity.dal.mapper.SysUserRoleMapper;
import com.bddk.geocourse.module.identity.stp.StpSchoolAdminUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class FrontendRosterService {

    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_INACTIVE = 0;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public FrontendRosterService(SysUserMapper sysUserMapper,
                                 SysRoleMapper sysRoleMapper,
                                 SysUserRoleMapper sysUserRoleMapper,
                                 ObjectMapper objectMapper) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.objectMapper = objectMapper;
    }

    public List<RosterTeacherView> listTeachers() {
        requireSchoolAdminLogin();
        return listUsersByRole(requiredTenantId(), "teacher").stream().map(this::toTeacherView).toList();
    }

    public List<RosterStudentView> listStudents() {
        requireSchoolAdminLogin();
        return listUsersByRole(requiredTenantId(), "student").stream().map(this::toStudentView).toList();
    }

    public RosterTeacherView createTeacher(RosterTeacherCreateRequest request, String schoolName) {
        requireSchoolAdminLogin();
        SysRoleDO role = requireRole(requiredTenantId(), "teacher");
        SysUserDO user = buildBaseUser(request.username(), request.initialPassword(), request.name(), request.mobile(), request.email(), schoolName);
        user.setUserType("teacher");
        user.setRegisterSource("school-roster");
        sysUserMapper.insert(user);
        insertUserRole(user, role);
        return toTeacherView(user, request.initialPassword());
    }

    public RosterStudentView createStudent(RosterStudentCreateRequest request, String schoolName) {
        requireSchoolAdminLogin();
        SysRoleDO role = requireRole(requiredTenantId(), "student");
        SysUserDO user = buildBaseUser(request.username(), request.initialPassword(), request.name(), request.mobile(), request.email(), schoolName);
        user.setUserType("student");
        user.setRegisterSource("school-roster");
        user.setUserNo(StringUtils.hasText(request.studentNo()) ? request.studentNo().trim() : generateUserNo("S"));
        FrontendUserMetadata metadata = readMetadata(user);
        metadata.setGradeClass(trimToNull(request.gradeClass()));
        user.setRemark(writeMetadata(metadata));
        sysUserMapper.insert(user);
        insertUserRole(user, role);
        return toStudentView(user, request.initialPassword());
    }

    public RosterTeacherView updateTeacherStatus(Long userId, String status) {
        requireSchoolAdminLogin();
        SysUserDO user = requireUserWithRole(userId, "teacher");
        user.setStatus(parseStatus(status));
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
        return toTeacherView(user);
    }

    public RosterStudentView updateStudentStatus(Long userId, String status) {
        requireSchoolAdminLogin();
        SysUserDO user = requireUserWithRole(userId, "student");
        user.setStatus(parseStatus(status));
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
        return toStudentView(user);
    }

    public int countTeachers(Long tenantId) {
        return listUsersByRole(tenantId, "teacher").size();
    }

    public int countStudents(Long tenantId) {
        return listUsersByRole(tenantId, "student").size();
    }

    private List<SysUserDO> listUsersByRole(Long tenantId, String roleCode) {
        SysRoleDO role = requireRole(tenantId, roleCode);
        List<Long> userIds = sysUserRoleMapper.selectList(Wrappers.<SysUserRoleDO>lambdaQuery()
                        .eq(SysUserRoleDO::getTenantId, tenantId)
                        .eq(SysUserRoleDO::getRoleId, role.getId())
                        .eq(SysUserRoleDO::getStatus, STATUS_ACTIVE))
                .stream()
                .map(SysUserRoleDO::getUserId)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return List.of();
        }
        return sysUserMapper.selectList(Wrappers.<SysUserDO>lambdaQuery()
                        .eq(SysUserDO::getTenantId, tenantId)
                        .in(SysUserDO::getId, userIds)
                        .orderByDesc(SysUserDO::getCreateTime)
                        .orderByDesc(SysUserDO::getId))
                .stream()
                .toList();
    }

    private SysUserDO buildBaseUser(String rawUsername,
                                    String rawPassword,
                                    String rawName,
                                    String rawMobile,
                                    String rawEmail,
                                    String schoolName) {
        Long tenantId = requiredTenantId();
        String username = requireText(rawUsername, "Username is required");
        String password = requireText(rawPassword, "Initial password is required");
        if (password.length() < 6) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Initial password must be at least 6 characters");
        }
        ensureUsernameAvailable(tenantId, username);
        SysUserDO user = new SysUserDO();
        user.setTenantId(tenantId);
        user.setUsername(username);
        user.setUserNo(generateUserNo("U"));
        user.setNickname(StringUtils.hasText(rawName) ? rawName.trim() : username);
        user.setRealName(trimToNull(rawName));
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setPhone(requireText(rawMobile, "Mobile is required"));
        user.setEmail(trimToNull(rawEmail));
        user.setStatus(STATUS_ACTIVE);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        FrontendUserMetadata metadata = new FrontendUserMetadata();
        metadata.setSchoolName(trimToNull(schoolName));
        user.setRemark(writeMetadata(metadata));
        return user;
    }

    private void insertUserRole(SysUserDO user, SysRoleDO role) {
        SysUserRoleDO userRole = new SysUserRoleDO();
        userRole.setTenantId(user.getTenantId());
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        userRole.setStatus(STATUS_ACTIVE);
        userRole.setCreateTime(LocalDateTime.now());
        userRole.setUpdateTime(LocalDateTime.now());
        sysUserRoleMapper.insert(userRole);
    }

    private SysUserDO requireUserWithRole(Long userId, String roleCode) {
        Long tenantId = requiredTenantId();
        SysUserDO user = sysUserMapper.selectOne(Wrappers.<SysUserDO>lambdaQuery()
                .eq(SysUserDO::getTenantId, tenantId)
                .eq(SysUserDO::getId, userId)
                .last("limit 1"));
        if (user == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "User does not exist");
        }
        boolean hasRole = listUsersByRole(tenantId, roleCode).stream().anyMatch(item -> Objects.equals(item.getId(), userId));
        if (!hasRole) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "User does not belong to the requested roster");
        }
        return user;
    }

    private SysRoleDO requireRole(Long tenantId, String roleCode) {
        SysRoleDO role = sysRoleMapper.selectOne(Wrappers.<SysRoleDO>lambdaQuery()
                .eq(SysRoleDO::getTenantId, tenantId)
                .eq(SysRoleDO::getRoleCode, roleCode)
                .eq(SysRoleDO::getStatus, STATUS_ACTIVE)
                .last("limit 1"));
        if (role == null) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Role does not exist: " + roleCode);
        }
        return role;
    }

    private void ensureUsernameAvailable(Long tenantId, String username) {
        Long count = sysUserMapper.selectCount(Wrappers.<SysUserDO>lambdaQuery()
                .eq(SysUserDO::getTenantId, tenantId)
                .eq(SysUserDO::getUsername, username));
        if (count != null && count > 0) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Username already exists");
        }
    }

    private int parseStatus(String status) {
        return "inactive".equalsIgnoreCase(trimToNull(status)) ? STATUS_INACTIVE : STATUS_ACTIVE;
    }

    private RosterTeacherView toTeacherView(SysUserDO user) {
        return toTeacherView(user, null);
    }

    private RosterTeacherView toTeacherView(SysUserDO user, String initialPassword) {
        String name = StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getNickname();
        return new RosterTeacherView(
                user.getId(),
                user.getUsername(),
                initialPassword,
                name,
                nullSafe(user.getPhone()),
                nullSafe(user.getEmail()),
                user.getStatus() != null && user.getStatus() == STATUS_ACTIVE ? "active" : "inactive",
                formatTime(user.getCreateTime()),
                null,
                null,
                null
        );
    }

    private RosterStudentView toStudentView(SysUserDO user) {
        return toStudentView(user, null);
    }

    private RosterStudentView toStudentView(SysUserDO user, String initialPassword) {
        FrontendUserMetadata metadata = readMetadata(user);
        String name = StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getNickname();
        return new RosterStudentView(
                user.getId(),
                user.getUsername(),
                initialPassword,
                name,
                nullSafe(user.getPhone()),
                nullSafe(user.getEmail()),
                nullSafe(user.getUserNo()),
                metadata.getGradeClass(),
                user.getStatus() != null && user.getStatus() == STATUS_ACTIVE ? "active" : "inactive",
                formatTime(user.getCreateTime())
        );
    }

    private FrontendUserMetadata readMetadata(SysUserDO user) {
        if (!StringUtils.hasText(user.getRemark())) {
            return new FrontendUserMetadata();
        }
        try {
            return objectMapper.readValue(user.getRemark(), FrontendUserMetadata.class);
        } catch (Exception ignored) {
            return new FrontendUserMetadata();
        }
    }

    private String writeMetadata(FrontendUserMetadata metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException ex) {
            throw new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to serialize user metadata");
        }
    }

    private void requireSchoolAdminLogin() {
        if (!StpSchoolAdminUtil.stpLogic.isLogin()) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED);
        }
    }

    private Long requiredTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new ServiceException(ErrorCode.TENANT_REQUIRED);
        }
        return tenantId;
    }

    private String requireText(String value, String message) {
        String text = trimToNull(value);
        if (!StringUtils.hasText(text)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, message);
        }
        return text;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String formatTime(LocalDateTime value) {
        return value == null ? "" : DATETIME_FORMATTER.format(value);
    }

    private String generateUserNo(String prefix) {
        return prefix + System.currentTimeMillis();
    }
}
