package com.bddk.geocourse.module.compat.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.framework.tenant.TenantContextHolder;
import com.bddk.geocourse.module.compat.model.FrontendAuthLoginRequest;
import com.bddk.geocourse.module.compat.model.FrontendAuthLoginResponse;
import com.bddk.geocourse.module.compat.model.FrontendAuthRegisterRequest;
import com.bddk.geocourse.module.compat.model.FrontendAuthUserView;
import com.bddk.geocourse.module.compat.model.FrontendUserMetadata;
import com.bddk.geocourse.module.course.service.CourseFileStorageService;
import com.bddk.geocourse.module.course.service.StoredResource;
import com.bddk.geocourse.module.identity.dal.dataobject.SysRoleDO;
import com.bddk.geocourse.module.identity.dal.dataobject.SysUserDO;
import com.bddk.geocourse.module.identity.dal.dataobject.SysUserRoleDO;
import com.bddk.geocourse.module.identity.dal.mapper.SysRoleMapper;
import com.bddk.geocourse.module.identity.dal.mapper.SysUserMapper;
import com.bddk.geocourse.module.identity.dal.mapper.SysUserRoleMapper;
import com.bddk.geocourse.module.identity.model.AdminLoginRequest;
import com.bddk.geocourse.module.identity.model.AdminLoginResult;
import com.bddk.geocourse.module.identity.service.AdminAuthService;
import com.bddk.geocourse.module.identity.service.ConsumerAuthService;
import com.bddk.geocourse.module.identity.service.SchoolAdminAuthService;
import com.bddk.geocourse.module.identity.service.StudentAuthService;
import com.bddk.geocourse.module.identity.service.TeacherAuthService;
import com.bddk.geocourse.module.identity.stp.StpAdminUtil;
import com.bddk.geocourse.module.identity.stp.StpConsumerUtil;
import com.bddk.geocourse.module.identity.stp.StpSchoolAdminUtil;
import com.bddk.geocourse.module.identity.stp.StpStudentUtil;
import com.bddk.geocourse.module.identity.stp.StpTeacherUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class FrontendAuthService {

    private static final int STATUS_ACTIVE = 1;

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final TeacherAuthService teacherAuthService;
    private final StudentAuthService studentAuthService;
    private final SchoolAdminAuthService schoolAdminAuthService;
    private final ConsumerAuthService consumerAuthService;
    private final AdminAuthService adminAuthService;
    private final FrontendSchoolPortalService frontendSchoolPortalService;
    private final CourseFileStorageService courseFileStorageService;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public FrontendAuthService(SysUserMapper sysUserMapper,
                               SysRoleMapper sysRoleMapper,
                               SysUserRoleMapper sysUserRoleMapper,
                               TeacherAuthService teacherAuthService,
                               StudentAuthService studentAuthService,
                               SchoolAdminAuthService schoolAdminAuthService,
                               ConsumerAuthService consumerAuthService,
                               AdminAuthService adminAuthService,
                               FrontendSchoolPortalService frontendSchoolPortalService,
                               CourseFileStorageService courseFileStorageService,
                               ObjectMapper objectMapper) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.teacherAuthService = teacherAuthService;
        this.studentAuthService = studentAuthService;
        this.schoolAdminAuthService = schoolAdminAuthService;
        this.consumerAuthService = consumerAuthService;
        this.adminAuthService = adminAuthService;
        this.frontendSchoolPortalService = frontendSchoolPortalService;
        this.courseFileStorageService = courseFileStorageService;
        this.objectMapper = objectMapper;
    }

    public FrontendAuthLoginResponse login(FrontendAuthLoginRequest request) {
        String portal = choosePortal(request.username(), request.password(), request.school());
        AdminLoginResult result = performLogin(portal, request.username(), request.password());
        SysUserDO user = requireUser(result.loginId());
        FrontendUserMetadata metadata = readMetadata(user);
        String schoolName = normalizeSchoolName(user.getTenantId(), request.school(), metadata.getSchoolName());
        if (StringUtils.hasText(request.school()) && !StringUtils.hasText(metadata.getSchoolName())) {
            metadata.setSchoolName(schoolName);
            persistMetadata(user, metadata);
        }
        return new FrontendAuthLoginResponse(result.tokenValue(), buildUserView(user, portal, schoolName, listRoleCodes(user)));
    }

    public FrontendAuthLoginResponse register(FrontendAuthRegisterRequest request) {
        String roleCode = normalizeRegisterRole(request.role());
        if ("admin".equals(roleCode)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Self-service super admin registration is not supported");
        }
        Long tenantId = requiredTenantId();
        String username = requireText(request.username(), "Username is required");
        String password = requireText(request.password(), "Password is required");
        if (password.length() < 6) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Password must be at least 6 characters");
        }
        ensureUsernameAvailable(tenantId, username);

        SysRoleDO role = requireRole(tenantId, roleCode);
        String schoolName = normalizeSchoolName(tenantId, request.school(), null);
        if ("school_admin".equals(roleCode) && !StringUtils.hasText(schoolName)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "School name is required for school admin registration");
        }

        FrontendUserMetadata metadata = new FrontendUserMetadata();
        metadata.setSchoolName(schoolName);

        SysUserDO user = new SysUserDO();
        user.setTenantId(tenantId);
        user.setUserNo(generateUserNo(roleCode));
        user.setUsername(username);
        user.setNickname(username);
        user.setRealName(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setUserType(resolveUserType(roleCode));
        user.setRegisterSource("frontend-register");
        user.setStatus(STATUS_ACTIVE);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setRemark(writeMetadata(metadata));
        sysUserMapper.insert(user);

        SysUserRoleDO userRole = new SysUserRoleDO();
        userRole.setTenantId(tenantId);
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        userRole.setStatus(STATUS_ACTIVE);
        userRole.setCreateTime(LocalDateTime.now());
        userRole.setUpdateTime(LocalDateTime.now());
        sysUserRoleMapper.insert(userRole);

        String portal = portalByRoleCode(roleCode);
        AdminLoginResult result = performLogin(portal, username, password);
        return new FrontendAuthLoginResponse(result.tokenValue(), buildUserView(user, portal, schoolName, List.of(roleCode)));
    }

    public FrontendAuthUserView currentUser() {
        CurrentLogin current = resolveCurrentLogin();
        SysUserDO user = requireUser(current.userId());
        return buildUserView(user, current.portal(), null, listRoleCodes(user));
    }

    public FrontendAuthUserView requireCurrentSchoolAdmin() {
        CurrentLogin current = resolveCurrentLogin();
        if (!"school-admin".equals(current.portal())) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "School admin login is required");
        }
        SysUserDO user = requireUser(current.userId());
        return buildUserView(user, current.portal(), null, listRoleCodes(user));
    }

    public FrontendAuthUserView updateTeacherProfile(String displayName, MultipartFile avatarFile, boolean clearAvatar) {
        CurrentLogin current = resolveCurrentLogin();
        if (!"teacher".equals(current.portal())) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "Teacher login is required");
        }
        SysUserDO user = requireUser(current.userId());
        user.setRealName(requireText(displayName, "Display name is required"));
        user.setNickname(user.getRealName());
        if (clearAvatar && StringUtils.hasText(user.getAvatarUrl())) {
            courseFileStorageService.deleteByUrl(user.getAvatarUrl());
            user.setAvatarUrl(null);
        }
        if (avatarFile != null && !avatarFile.isEmpty()) {
            if (StringUtils.hasText(user.getAvatarUrl())) {
                courseFileStorageService.deleteByUrl(user.getAvatarUrl());
            }
            StoredResource storedResource = courseFileStorageService.store("profile", "avatar", avatarFile);
            user.setAvatarUrl(storedResource == null ? null : storedResource.url());
        }
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
        return buildUserView(user, current.portal(), null, listRoleCodes(user));
    }

    public Long currentTenantId() {
        return requiredTenantId();
    }

    private String choosePortal(String username, String password, String school) {
        if (StringUtils.hasText(school)) {
            return "school-admin";
        }
        Long tenantId = requiredTenantId();
        SysUserDO user = sysUserMapper.selectOne(Wrappers.<SysUserDO>lambdaQuery()
                .eq(SysUserDO::getTenantId, tenantId)
                .eq(SysUserDO::getUsername, username)
                .eq(SysUserDO::getStatus, STATUS_ACTIVE)
                .last("limit 1"));
        if (user == null || !matchesPassword(password, user.getPasswordHash())) {
            throw new ServiceException(ErrorCode.AUTH_LOGIN_FAILED);
        }
        List<String> roleCodes = listRoleCodes(user);
        if (roleCodes.contains("admin")) {
            return "admin";
        }
        if (roleCodes.contains("school_admin")) {
            return "school-admin";
        }
        if (roleCodes.contains("teacher")) {
            return "teacher";
        }
        if (roleCodes.contains("student")) {
            return "student";
        }
        if (roleCodes.contains("consumer")) {
            return "consumer";
        }
        throw new ServiceException(ErrorCode.AUTH_LOGIN_FAILED);
    }

    private AdminLoginResult performLogin(String portal, String username, String password) {
        AdminLoginRequest request = new AdminLoginRequest(username, password);
        return switch (portal) {
            case "admin" -> adminAuthService.login(request);
            case "school-admin" -> schoolAdminAuthService.login(request);
            case "teacher" -> teacherAuthService.login(request);
            case "student" -> studentAuthService.login(request);
            case "consumer" -> consumerAuthService.login(request);
            default -> throw new ServiceException(ErrorCode.BAD_REQUEST, "Unsupported portal: " + portal);
        };
    }

    private CurrentLogin resolveCurrentLogin() {
        if (StpAdminUtil.stpLogic.isLogin()) {
            return new CurrentLogin("admin", StpAdminUtil.stpLogic.getLoginIdAsLong());
        }
        if (StpSchoolAdminUtil.stpLogic.isLogin()) {
            return new CurrentLogin("school-admin", StpSchoolAdminUtil.stpLogic.getLoginIdAsLong());
        }
        if (StpTeacherUtil.stpLogic.isLogin()) {
            return new CurrentLogin("teacher", StpTeacherUtil.stpLogic.getLoginIdAsLong());
        }
        if (StpStudentUtil.stpLogic.isLogin()) {
            return new CurrentLogin("student", StpStudentUtil.stpLogic.getLoginIdAsLong());
        }
        if (StpConsumerUtil.stpLogic.isLogin()) {
            return new CurrentLogin("consumer", StpConsumerUtil.stpLogic.getLoginIdAsLong());
        }
        throw new ServiceException(ErrorCode.UNAUTHORIZED);
    }

    private FrontendAuthUserView buildUserView(SysUserDO user,
                                               String portal,
                                               String preferredSchool,
                                               List<String> roleCodes) {
        FrontendUserMetadata metadata = readMetadata(user);
        String schoolName = normalizeSchoolName(user.getTenantId(), preferredSchool, metadata.getSchoolName());
        String displayName = StringUtils.hasText(user.getRealName())
                ? user.getRealName()
                : StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
        return new FrontendAuthUserView(
                user.getId(),
                user.getUsername(),
                schoolName,
                displayName,
                user.getAvatarUrl(),
                resolveFrontendRole(portal, roleCodes)
        );
    }

    private String resolveFrontendRole(String portal, List<String> roleCodes) {
        if (StringUtils.hasText(portal)) {
            return switch (portal) {
                case "admin" -> "SUPER_ADMIN";
                case "school-admin" -> "SCHOOL_ADMIN";
                case "teacher" -> "TEACHER";
                case "student" -> "STUDENT";
                case "consumer" -> "VISITOR";
                default -> "VISITOR";
            };
        }
        if (roleCodes.contains("admin")) {
            return "SUPER_ADMIN";
        }
        if (roleCodes.contains("school_admin")) {
            return "SCHOOL_ADMIN";
        }
        if (roleCodes.contains("teacher")) {
            return "TEACHER";
        }
        if (roleCodes.contains("student")) {
            return "STUDENT";
        }
        return "VISITOR";
    }

    private String normalizeRegisterRole(String rawRole) {
        String role = requireText(rawRole, "Role is required").toUpperCase(Locale.ROOT);
        return switch (role) {
            case "SCHOOL_ADMIN", "ADMIN" -> "school_admin";
            case "TEACHER" -> "teacher";
            case "STUDENT" -> "student";
            case "VISITOR" -> "consumer";
            case "SUPER_ADMIN" -> "admin";
            default -> throw new ServiceException(ErrorCode.BAD_REQUEST, "Unsupported role: " + rawRole);
        };
    }

    private String portalByRoleCode(String roleCode) {
        return switch (roleCode) {
            case "school_admin" -> "school-admin";
            case "teacher" -> "teacher";
            case "student" -> "student";
            case "consumer" -> "consumer";
            case "admin" -> "admin";
            default -> throw new ServiceException(ErrorCode.BAD_REQUEST, "Unsupported role code: " + roleCode);
        };
    }

    private String resolveUserType(String roleCode) {
        return switch (roleCode) {
            case "teacher" -> "teacher";
            case "student" -> "student";
            case "consumer" -> "consumer";
            default -> "admin";
        };
    }

    private List<String> listRoleCodes(SysUserDO user) {
        List<Long> roleIds = sysUserRoleMapper.selectList(Wrappers.<SysUserRoleDO>lambdaQuery()
                        .eq(SysUserRoleDO::getTenantId, user.getTenantId())
                        .eq(SysUserRoleDO::getUserId, user.getId())
                        .eq(SysUserRoleDO::getStatus, STATUS_ACTIVE))
                .stream()
                .map(SysUserRoleDO::getRoleId)
                .distinct()
                .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return sysRoleMapper.selectList(Wrappers.<SysRoleDO>lambdaQuery()
                        .eq(SysRoleDO::getTenantId, user.getTenantId())
                        .in(SysRoleDO::getId, roleIds)
                        .eq(SysRoleDO::getStatus, STATUS_ACTIVE))
                .stream()
                .map(SysRoleDO::getRoleCode)
                .filter(StringUtils::hasText)
                .toList();
    }

    private SysRoleDO requireRole(Long tenantId, String roleCode) {
        SysRoleDO role = sysRoleMapper.selectOne(Wrappers.<SysRoleDO>lambdaQuery()
                .eq(SysRoleDO::getTenantId, tenantId)
                .eq(SysRoleDO::getRoleCode, roleCode)
                .eq(SysRoleDO::getStatus, STATUS_ACTIVE)
                .last("limit 1"));
        if (role == null) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Role does not exist for current tenant: " + roleCode);
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

    private SysUserDO requireUser(Long userId) {
        SysUserDO user = sysUserMapper.selectOne(Wrappers.<SysUserDO>lambdaQuery()
                .eq(SysUserDO::getId, userId)
                .last("limit 1"));
        if (user == null) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED);
        }
        return user;
    }

    private boolean matchesPassword(String rawPassword, String passwordHash) {
        if (!StringUtils.hasText(passwordHash)) {
            return false;
        }
        if (passwordHash.startsWith("$2a$") || passwordHash.startsWith("$2b$") || passwordHash.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, passwordHash);
        }
        return Objects.equals(rawPassword, passwordHash);
    }

    private Long requiredTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new ServiceException(ErrorCode.TENANT_REQUIRED);
        }
        return tenantId;
    }

    private String requireText(String value, String message) {
        String text = value == null ? null : value.trim();
        if (!StringUtils.hasText(text)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, message);
        }
        return text;
    }

    private String generateUserNo(String roleCode) {
        String prefix = switch (roleCode) {
            case "teacher" -> "T";
            case "student" -> "S";
            case "consumer" -> "C";
            case "school_admin" -> "A";
            default -> "U";
        };
        return prefix + System.currentTimeMillis();
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

    private void persistMetadata(SysUserDO user, FrontendUserMetadata metadata) {
        user.setRemark(writeMetadata(metadata));
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    private String normalizeSchoolName(Long tenantId, String preferredSchool, String metadataSchool) {
        if (StringUtils.hasText(preferredSchool)) {
            return preferredSchool.trim();
        }
        if (StringUtils.hasText(metadataSchool)) {
            return metadataSchool.trim();
        }
        return frontendSchoolPortalService.findConfiguredSchoolName(tenantId);
    }

    private record CurrentLogin(String portal, Long userId) {
    }
}
