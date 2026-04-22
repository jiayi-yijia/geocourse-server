package com.bddk.geocourse.module.classroom.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.module.assignment.entity.TeacherExamPublishTarget;
import com.bddk.geocourse.module.assignment.mapper.TeacherExamPublishTargetMapper;
import com.bddk.geocourse.module.classroom.dal.dataobject.SchoolClassCourseDO;
import com.bddk.geocourse.module.classroom.dal.dataobject.SchoolClassDO;
import com.bddk.geocourse.module.classroom.dal.dataobject.SchoolClassStudentDO;
import com.bddk.geocourse.module.classroom.dal.dataobject.SchoolClassTeacherDO;
import com.bddk.geocourse.module.classroom.dal.mapper.SchoolClassCourseMapper;
import com.bddk.geocourse.module.classroom.dal.mapper.SchoolClassMapper;
import com.bddk.geocourse.module.classroom.dal.mapper.SchoolClassStudentMapper;
import com.bddk.geocourse.module.classroom.dal.mapper.SchoolClassTeacherMapper;
import com.bddk.geocourse.module.classroom.model.SchoolAdminClassQuery;
import com.bddk.geocourse.module.classroom.model.SchoolAdminClassStudentQuery;
import com.bddk.geocourse.module.classroom.model.SchoolAdminClassViews;
import com.bddk.geocourse.module.classroom.service.SchoolAdminClassService;
import com.bddk.geocourse.module.compat.model.FrontendAuthUserView;
import com.bddk.geocourse.module.compat.service.FrontendAuthService;
import com.bddk.geocourse.module.course.dal.dataobject.CourseInfoDO;
import com.bddk.geocourse.module.course.dal.dataobject.CourseResourceDO;
import com.bddk.geocourse.module.course.dal.mapper.CourseInfoMapper;
import com.bddk.geocourse.module.course.dal.mapper.CourseResourceMapper;
import com.bddk.geocourse.module.identity.dal.dataobject.SysUserDO;
import com.bddk.geocourse.module.identity.dal.mapper.SysUserMapper;
import com.bddk.geocourse.module.lessonprep.dal.dataobject.LessonPrepDocumentDO;
import com.bddk.geocourse.module.lessonprep.dal.mapper.LessonPrepDocumentMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SchoolAdminClassServiceImpl implements SchoolAdminClassService {

    private static final int MAX_PAGE_SIZE = 200;
    private static final int ACTIVE_STATUS = 1;
    private static final String JOIN_STATUS_ACTIVE = "ACTIVE";
    private static final String JOIN_STATUS_LEFT = "LEFT";
    private static final String TARGET_TYPE_CLASS = "CLASS";

    private final FrontendAuthService frontendAuthService;
    private final SchoolClassMapper schoolClassMapper;
    private final SchoolClassTeacherMapper schoolClassTeacherMapper;
    private final SchoolClassStudentMapper schoolClassStudentMapper;
    private final SchoolClassCourseMapper schoolClassCourseMapper;
    private final SysUserMapper sysUserMapper;
    private final CourseInfoMapper courseInfoMapper;
    private final CourseResourceMapper courseResourceMapper;
    private final LessonPrepDocumentMapper lessonPrepDocumentMapper;
    private final TeacherExamPublishTargetMapper teacherExamPublishTargetMapper;

    public SchoolAdminClassServiceImpl(FrontendAuthService frontendAuthService,
                                       SchoolClassMapper schoolClassMapper,
                                       SchoolClassTeacherMapper schoolClassTeacherMapper,
                                       SchoolClassStudentMapper schoolClassStudentMapper,
                                       SchoolClassCourseMapper schoolClassCourseMapper,
                                       SysUserMapper sysUserMapper,
                                       CourseInfoMapper courseInfoMapper,
                                       CourseResourceMapper courseResourceMapper,
                                       LessonPrepDocumentMapper lessonPrepDocumentMapper,
                                       TeacherExamPublishTargetMapper teacherExamPublishTargetMapper) {
        this.frontendAuthService = frontendAuthService;
        this.schoolClassMapper = schoolClassMapper;
        this.schoolClassTeacherMapper = schoolClassTeacherMapper;
        this.schoolClassStudentMapper = schoolClassStudentMapper;
        this.schoolClassCourseMapper = schoolClassCourseMapper;
        this.sysUserMapper = sysUserMapper;
        this.courseInfoMapper = courseInfoMapper;
        this.courseResourceMapper = courseResourceMapper;
        this.lessonPrepDocumentMapper = lessonPrepDocumentMapper;
        this.teacherExamPublishTargetMapper = teacherExamPublishTargetMapper;
    }

    @Override
    public PageResult<SchoolAdminClassViews.Item> pageClasses(SchoolAdminClassQuery query) {
        requireCurrentSchoolAdmin();
        Long tenantId = frontendAuthService.currentTenantId();
        long pageNo = normalizePageNo(query.getPageNo());
        long pageSize = normalizePageSize(query.getPageSize());
        String keyword = normalizeText(query.getKeyword());
        String status = normalizeCode(query.getStatus());

        List<SchoolClassDO> classes = schoolClassMapper.selectList(Wrappers.<SchoolClassDO>lambdaQuery()
                .eq(SchoolClassDO::getTenantId, tenantId)
                .eq(StringUtils.hasText(status), SchoolClassDO::getStatus, status)
                .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(SchoolClassDO::getClassName, keyword)
                        .or()
                        .like(SchoolClassDO::getClassCode, keyword))
                .orderByDesc(SchoolClassDO::getUpdateTime)
                .orderByDesc(SchoolClassDO::getId));
        if (classes.isEmpty()) {
            return PageResult.of(List.of(), 0, pageNo, pageSize);
        }

        List<Long> classIds = classes.stream().map(SchoolClassDO::getId).toList();
        Map<Long, Long> activeStudentCountMap = loadActiveStudentCountMap(tenantId, classIds);
        Map<Long, Long> courseCountMap = loadCourseCountMap(tenantId, classIds);
        Map<Long, SysUserDO> teacherMap = loadUsersByIds(tenantId, classes.stream()
                .map(SchoolClassDO::getHomeroomTeacherId)
                .filter(Objects::nonNull)
                .toList());

        List<SchoolAdminClassViews.Item> views = classes.stream()
                .map(item -> new SchoolAdminClassViews.Item(
                        item.getId(),
                        item.getClassCode(),
                        item.getClassName(),
                        item.getStageCode(),
                        item.getGradeCode(),
                        normalizeCode(item.getStatus()),
                        item.getHomeroomTeacherId(),
                        resolveDisplayName(teacherMap.get(item.getHomeroomTeacherId())),
                        activeStudentCountMap.getOrDefault(item.getId(), 0L),
                        courseCountMap.getOrDefault(item.getId(), 0L),
                        item.getResourceSpaceId(),
                        item.getCreateTime()))
                .toList();
        return pageResult(views, pageNo, pageSize);
    }

    @Override
    public SchoolAdminClassViews.Detail getClassDetail(Long classId) {
        requireCurrentSchoolAdmin();
        Long tenantId = frontendAuthService.currentTenantId();
        SchoolClassDO schoolClass = getClassOrThrow(tenantId, classId);

        List<SchoolClassTeacherDO> teacherRelations = schoolClassTeacherMapper.selectList(Wrappers.<SchoolClassTeacherDO>lambdaQuery()
                .eq(SchoolClassTeacherDO::getTenantId, tenantId)
                .eq(SchoolClassTeacherDO::getClassId, classId)
                .orderByDesc(SchoolClassTeacherDO::getIsPrimary)
                .orderByAsc(SchoolClassTeacherDO::getId));
        Map<Long, SysUserDO> teacherMap = loadUsersByIds(tenantId, teacherRelations.stream()
                .map(SchoolClassTeacherDO::getTeacherId)
                .toList());

        List<SchoolClassCourseDO> courseRelations = schoolClassCourseMapper.selectList(Wrappers.<SchoolClassCourseDO>lambdaQuery()
                .eq(SchoolClassCourseDO::getTenantId, tenantId)
                .eq(SchoolClassCourseDO::getClassId, classId)
                .orderByAsc(SchoolClassCourseDO::getId));
        List<Long> courseIds = courseRelations.stream()
                .map(SchoolClassCourseDO::getCourseId)
                .distinct()
                .toList();
        Map<Long, CourseInfoDO> courseMap = loadCoursesByIds(tenantId, courseIds);

        List<SchoolClassStudentDO> studentRelations = schoolClassStudentMapper.selectList(Wrappers.<SchoolClassStudentDO>lambdaQuery()
                .eq(SchoolClassStudentDO::getTenantId, tenantId)
                .eq(SchoolClassStudentDO::getClassId, classId)
                .orderByDesc(SchoolClassStudentDO::getJoinedTime)
                .orderByDesc(SchoolClassStudentDO::getId));

        Long homeroomTeacherId = schoolClass.getHomeroomTeacherId() != null
                ? schoolClass.getHomeroomTeacherId()
                : teacherRelations.stream()
                .filter(item -> "HOMEROOM".equals(normalizeTeacherRoleCode(item.getRoleCode())))
                .map(SchoolClassTeacherDO::getTeacherId)
                .findFirst()
                .orElse(null);

        SchoolAdminClassViews.Stats stats = buildStats(tenantId, classId, courseIds, teacherRelations, studentRelations);
        return new SchoolAdminClassViews.Detail(
                schoolClass.getId(),
                schoolClass.getClassCode(),
                schoolClass.getClassName(),
                schoolClass.getStageCode(),
                schoolClass.getGradeCode(),
                schoolClass.getEnrollmentYear(),
                schoolClass.getHeadcount(),
                normalizeCode(schoolClass.getStatus()),
                schoolClass.getSchoolId(),
                homeroomTeacherId,
                resolveDisplayName(teacherMap.get(homeroomTeacherId)),
                schoolClass.getResourceSpaceId(),
                buildResourceSpacePath(schoolClass.getResourceSpaceId()),
                schoolClass.getRemark(),
                schoolClass.getCreateTime(),
                schoolClass.getUpdateTime(),
                teacherRelations.stream()
                        .map(item -> new SchoolAdminClassViews.Teacher(
                                item.getTeacherId(),
                                resolveDisplayName(teacherMap.get(item.getTeacherId())),
                                teacherMap.containsKey(item.getTeacherId()) ? teacherMap.get(item.getTeacherId()).getUsername() : null,
                                normalizeTeacherRoleCode(item.getRoleCode()),
                                Boolean.TRUE.equals(item.getIsPrimary())))
                        .toList(),
                courseRelations.stream()
                        .map(item -> {
                            CourseInfoDO course = courseMap.get(item.getCourseId());
                            return new SchoolAdminClassViews.Course(
                                    item.getCourseId(),
                                    course == null ? null : course.getCourseCode(),
                                    course == null ? null : course.getCourseName(),
                                    normalizeCourseRelationType(item.getRelationType()));
                        })
                        .toList(),
                stats);
    }

    @Override
    public PageResult<SchoolAdminClassViews.Student> pageStudents(Long classId, SchoolAdminClassStudentQuery query) {
        requireCurrentSchoolAdmin();
        Long tenantId = frontendAuthService.currentTenantId();
        getClassOrThrow(tenantId, classId);

        long pageNo = normalizePageNo(query.getPageNo());
        long pageSize = normalizePageSize(query.getPageSize());
        String keyword = normalizeText(query.getKeyword());
        String joinStatus = normalizeCode(query.getJoinStatus());

        List<SchoolClassStudentDO> relations = schoolClassStudentMapper.selectList(Wrappers.<SchoolClassStudentDO>lambdaQuery()
                .eq(SchoolClassStudentDO::getTenantId, tenantId)
                .eq(SchoolClassStudentDO::getClassId, classId)
                .eq(StringUtils.hasText(joinStatus), SchoolClassStudentDO::getJoinStatus, joinStatus)
                .orderByDesc(SchoolClassStudentDO::getJoinedTime)
                .orderByDesc(SchoolClassStudentDO::getId));
        if (relations.isEmpty()) {
            return PageResult.of(List.of(), 0, pageNo, pageSize);
        }

        Map<Long, SysUserDO> studentMap = loadUsersByIds(tenantId, relations.stream()
                .map(SchoolClassStudentDO::getStudentId)
                .toList());
        List<SchoolAdminClassViews.Student> views = relations.stream()
                .map(item -> {
                    SysUserDO user = studentMap.get(item.getStudentId());
                    return new SchoolAdminClassViews.Student(
                            item.getStudentId(),
                            firstNonBlank(item.getStudentNo(), user == null ? null : user.getUserNo()),
                            resolveDisplayName(user),
                            user == null ? null : user.getUsername(),
                            user == null ? null : user.getPhone(),
                            normalizeCode(item.getJoinStatus()),
                            item.getJoinedTime(),
                            item.getLeftTime());
                })
                .filter(item -> matchesStudentKeyword(item, keyword))
                .toList();
        return pageResult(views, pageNo, pageSize);
    }

    @Override
    @Transactional
    public SchoolAdminClassViews.StudentImportResult importStudents(Long classId, MultipartFile file) {
        FrontendAuthUserView operator = requireCurrentSchoolAdmin();
        Long tenantId = frontendAuthService.currentTenantId();
        getClassOrThrow(tenantId, classId);
        if (file == null || file.isEmpty()) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "请选择要导入的 Excel 文件");
        }

        List<SchoolAdminClassViews.StudentImportFailure> failures = new ArrayList<>();
        int totalRows = 0;
        int addedCount = 0;
        int reactivatedCount = 0;
        int ignoredCount = 0;
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            if (workbook.getNumberOfSheets() == 0) {
                return new SchoolAdminClassViews.StudentImportResult(
                        file.getOriginalFilename(),
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        List.of());
            }
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> headerMap = parseHeader(sheet.getRow(sheet.getFirstRowNum()), formatter);
            if (!headerMap.containsKey("userId")
                    && !headerMap.containsKey("studentNo")
                    && !headerMap.containsKey("username")
                    && !headerMap.containsKey("phone")) {
                throw new ServiceException(ErrorCode.BAD_REQUEST, "导入模板表头不正确");
            }

            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                ImportRow importRow = readImportRow(row, headerMap, formatter);
                if (importRow == null) {
                    continue;
                }
                totalRows++;
                ImportOutcome outcome = handleImportRow(tenantId, classId, operator.id(), rowIndex + 1, importRow);
                if (outcome.failure() != null) {
                    failures.add(outcome.failure());
                    continue;
                }
                switch (outcome.status()) {
                    case ADDED -> addedCount++;
                    case REACTIVATED -> reactivatedCount++;
                    case IGNORED -> ignoredCount++;
                    default -> {
                    }
                }
            }
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "无法解析导入文件");
        }

        int successCount = addedCount + reactivatedCount;
        return new SchoolAdminClassViews.StudentImportResult(
                file.getOriginalFilename(),
                totalRows,
                addedCount,
                reactivatedCount,
                ignoredCount,
                successCount,
                failures.size(),
                failures);
    }

    @Override
    public byte[] downloadStudentImportTemplate() {
        requireCurrentSchoolAdmin();
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet dataSheet = workbook.createSheet("students");
            Row header = dataSheet.createRow(0);
            header.createCell(0).setCellValue("用户ID");
            header.createCell(1).setCellValue("学号");
            header.createCell(2).setCellValue("登录账号");
            header.createCell(3).setCellValue("手机号");
            header.createCell(4).setCellValue("姓名");
            for (int i = 0; i < 5; i++) {
                dataSheet.setColumnWidth(i, 18 * 256);
            }

            Sheet noteSheet = workbook.createSheet("说明");
            noteSheet.createRow(0).createCell(0).setCellValue("填写规则");
            noteSheet.createRow(1).createCell(0).setCellValue("请保留 students 工作表首行标题不变。");
            noteSheet.createRow(2).createCell(0).setCellValue("至少填写 用户ID / 学号 / 登录账号 / 手机号 其中一项。");
            noteSheet.createRow(3).createCell(0).setCellValue("姓名用于人工核对，可留空。");
            noteSheet.setColumnWidth(0, 48 * 256);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "生成导入模板失败");
        }
    }

    private FrontendAuthUserView requireCurrentSchoolAdmin() {
        return frontendAuthService.requireCurrentSchoolAdmin();
    }

    private SchoolClassDO getClassOrThrow(Long tenantId, Long classId) {
        SchoolClassDO schoolClass = schoolClassMapper.selectOne(Wrappers.<SchoolClassDO>lambdaQuery()
                .eq(SchoolClassDO::getTenantId, tenantId)
                .eq(SchoolClassDO::getId, classId)
                .last("limit 1"));
        if (schoolClass == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "班级不存在");
        }
        return schoolClass;
    }

    private SchoolAdminClassViews.Stats buildStats(Long tenantId,
                                                   Long classId,
                                                   List<Long> courseIds,
                                                   List<SchoolClassTeacherDO> teacherRelations,
                                                   List<SchoolClassStudentDO> studentRelations) {
        long activeStudentCount = studentRelations.stream()
                .filter(item -> JOIN_STATUS_ACTIVE.equals(normalizeCode(item.getJoinStatus())))
                .count();
        long leftStudentCount = studentRelations.stream()
                .filter(item -> JOIN_STATUS_LEFT.equals(normalizeCode(item.getJoinStatus())))
                .count();
        long teacherCount = teacherRelations.stream()
                .map(SchoolClassTeacherDO::getTeacherId)
                .distinct()
                .count();
        long courseCount = courseIds.size();
        long resourceCount = courseIds.isEmpty() ? 0 : safeCount(courseResourceMapper.selectCount(Wrappers.<CourseResourceDO>lambdaQuery()
                .eq(CourseResourceDO::getTenantId, tenantId)
                .in(CourseResourceDO::getCourseId, courseIds)
                .eq(CourseResourceDO::getStatus, ACTIVE_STATUS)));
        long lessonCount = courseIds.isEmpty() ? 0 : safeCount(lessonPrepDocumentMapper.selectCount(Wrappers.<LessonPrepDocumentDO>lambdaQuery()
                .eq(LessonPrepDocumentDO::getTenantId, tenantId)
                .eq(LessonPrepDocumentDO::getDocType, "LESSON_PLAN")
                .in(LessonPrepDocumentDO::getCourseId, courseIds)));
        long examCount = safeCount(teacherExamPublishTargetMapper.selectCount(Wrappers.<TeacherExamPublishTarget>lambdaQuery()
                .eq(TeacherExamPublishTarget::getTenantId, tenantId)
                .eq(TeacherExamPublishTarget::getTargetType, TARGET_TYPE_CLASS)
                .eq(TeacherExamPublishTarget::getTargetId, classId)));
        return new SchoolAdminClassViews.Stats(
                studentRelations.size(),
                activeStudentCount,
                leftStudentCount,
                teacherCount,
                courseCount,
                resourceCount,
                lessonCount,
                0,
                examCount);
    }

    private Map<Long, Long> loadActiveStudentCountMap(Long tenantId, List<Long> classIds) {
        if (classIds.isEmpty()) {
            return Map.of();
        }
        return schoolClassStudentMapper.selectList(Wrappers.<SchoolClassStudentDO>lambdaQuery()
                        .eq(SchoolClassStudentDO::getTenantId, tenantId)
                        .eq(SchoolClassStudentDO::getJoinStatus, JOIN_STATUS_ACTIVE)
                        .in(SchoolClassStudentDO::getClassId, classIds))
                .stream()
                .collect(Collectors.groupingBy(SchoolClassStudentDO::getClassId, Collectors.counting()));
    }

    private Map<Long, Long> loadCourseCountMap(Long tenantId, List<Long> classIds) {
        if (classIds.isEmpty()) {
            return Map.of();
        }
        return schoolClassCourseMapper.selectList(Wrappers.<SchoolClassCourseDO>lambdaQuery()
                        .eq(SchoolClassCourseDO::getTenantId, tenantId)
                        .in(SchoolClassCourseDO::getClassId, classIds))
                .stream()
                .collect(Collectors.groupingBy(
                        SchoolClassCourseDO::getClassId,
                        Collectors.collectingAndThen(
                                Collectors.mapping(SchoolClassCourseDO::getCourseId, Collectors.toSet()),
                                set -> (long) set.size())));
    }

    private Map<Long, SysUserDO> loadUsersByIds(Long tenantId, Collection<Long> userIds) {
        List<Long> ids = userIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return sysUserMapper.selectList(Wrappers.<SysUserDO>lambdaQuery()
                        .eq(SysUserDO::getTenantId, tenantId)
                        .in(SysUserDO::getId, ids))
                .stream()
                .collect(Collectors.toMap(SysUserDO::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, CourseInfoDO> loadCoursesByIds(Long tenantId, Collection<Long> courseIds) {
        List<Long> ids = courseIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return courseInfoMapper.selectList(Wrappers.<CourseInfoDO>lambdaQuery()
                        .eq(CourseInfoDO::getTenantId, tenantId)
                        .in(CourseInfoDO::getId, ids))
                .stream()
                .collect(Collectors.toMap(CourseInfoDO::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private boolean matchesStudentKeyword(SchoolAdminClassViews.Student item, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        return containsIgnoreCase(item.studentNo(), keyword)
                || containsIgnoreCase(item.studentName(), keyword)
                || containsIgnoreCase(item.username(), keyword)
                || containsIgnoreCase(item.phone(), keyword);
    }

    private <T> PageResult<T> pageResult(List<T> items, long pageNo, long pageSize) {
        long total = items.size();
        int fromIndex = (int) Math.min((pageNo - 1) * pageSize, total);
        int toIndex = (int) Math.min(fromIndex + pageSize, total);
        return PageResult.of(items.subList(fromIndex, toIndex), total, pageNo, pageSize);
    }

    private Map<String, Integer> parseHeader(Row headerRow, DataFormatter formatter) {
        if (headerRow == null) {
            return Map.of();
        }
        Map<String, Integer> headerMap = new LinkedHashMap<>();
        short lastCellNum = headerRow.getLastCellNum();
        for (int index = 0; index < lastCellNum; index++) {
            String normalized = normalizeHeader(formatter.formatCellValue(headerRow.getCell(index)));
            if (!StringUtils.hasText(normalized)) {
                continue;
            }
            switch (normalized) {
                case "userid", "用户id" -> headerMap.put("userId", index);
                case "studentno", "学号" -> headerMap.put("studentNo", index);
                case "username", "登录账号", "用户名", "账号" -> headerMap.put("username", index);
                case "phone", "手机号", "手机号码" -> headerMap.put("phone", index);
                case "name", "姓名", "学生姓名" -> headerMap.put("name", index);
                default -> {
                }
            }
        }
        return headerMap;
    }

    private ImportRow readImportRow(Row row, Map<String, Integer> headerMap, DataFormatter formatter) {
        if (row == null) {
            return null;
        }
        ImportRow importRow = new ImportRow(
                readCellText(row, headerMap.get("userId"), formatter),
                readCellText(row, headerMap.get("studentNo"), formatter),
                readCellText(row, headerMap.get("username"), formatter),
                readCellText(row, headerMap.get("phone"), formatter),
                readCellText(row, headerMap.get("name"), formatter));
        if (importRow.isEmpty()) {
            return null;
        }
        return importRow;
    }

    private ImportOutcome handleImportRow(Long tenantId,
                                          Long classId,
                                          Long operatorId,
                                          int rowNo,
                                          ImportRow row) {
        if (!row.hasAnyIdentifier()) {
            return ImportOutcome.failure(buildFailure(rowNo, row, null, "至少填写用户ID、学号、登录账号、手机号中的一项"));
        }

        List<SysUserDO> matchedUsers = new ArrayList<>();
        SysUserDO previewUser = null;

        if (StringUtils.hasText(row.userIdText())) {
            Long userId = parseLongValue(row.userIdText());
            if (userId == null) {
                return ImportOutcome.failure(buildFailure(rowNo, row, null, "用户ID格式不正确"));
            }
            StudentMatchResult result = findStudentById(tenantId, userId);
            if (!result.success()) {
                return ImportOutcome.failure(buildFailure(rowNo, row, previewUser, result.message()));
            }
            previewUser = result.user();
            matchedUsers.add(result.user());
        }
        if (StringUtils.hasText(row.studentNo())) {
            StudentMatchResult result = findStudentByUserNo(tenantId, row.studentNo());
            if (!result.success()) {
                return ImportOutcome.failure(buildFailure(rowNo, row, previewUser, result.message()));
            }
            previewUser = previewUser == null ? result.user() : previewUser;
            matchedUsers.add(result.user());
        }
        if (StringUtils.hasText(row.username())) {
            StudentMatchResult result = findStudentByUsername(tenantId, row.username());
            if (!result.success()) {
                return ImportOutcome.failure(buildFailure(rowNo, row, previewUser, result.message()));
            }
            previewUser = previewUser == null ? result.user() : previewUser;
            matchedUsers.add(result.user());
        }
        if (StringUtils.hasText(row.phone())) {
            StudentMatchResult result = findStudentByPhone(tenantId, row.phone());
            if (!result.success()) {
                return ImportOutcome.failure(buildFailure(rowNo, row, previewUser, result.message()));
            }
            previewUser = previewUser == null ? result.user() : previewUser;
            matchedUsers.add(result.user());
        }

        Set<Long> distinctUserIds = matchedUsers.stream()
                .map(SysUserDO::getId)
                .collect(Collectors.toSet());
        if (distinctUserIds.size() > 1) {
            return ImportOutcome.failure(buildFailure(rowNo, row, previewUser, "提供的标识匹配到了不同学生"));
        }
        if (matchedUsers.isEmpty()) {
            return ImportOutcome.failure(buildFailure(rowNo, row, null, "未匹配到学生"));
        }

        SysUserDO matchedUser = matchedUsers.get(0);
        SchoolClassStudentDO relation = schoolClassStudentMapper.selectOne(Wrappers.<SchoolClassStudentDO>lambdaQuery()
                .eq(SchoolClassStudentDO::getTenantId, tenantId)
                .eq(SchoolClassStudentDO::getClassId, classId)
                .eq(SchoolClassStudentDO::getStudentId, matchedUser.getId())
                .last("limit 1"));
        LocalDateTime now = LocalDateTime.now();
        String snapshotStudentNo = firstNonBlank(row.studentNo(), matchedUser.getUserNo());

        if (relation == null) {
            SchoolClassStudentDO created = new SchoolClassStudentDO();
            created.setTenantId(tenantId);
            created.setClassId(classId);
            created.setStudentId(matchedUser.getId());
            created.setStudentNo(snapshotStudentNo);
            created.setJoinStatus(JOIN_STATUS_ACTIVE);
            created.setJoinedTime(now);
            created.setCreateBy(operatorId);
            created.setUpdateBy(operatorId);
            created.setCreateTime(now);
            created.setUpdateTime(now);
            schoolClassStudentMapper.insert(created);
            return ImportOutcome.added();
        }

        String currentStatus = normalizeCode(relation.getJoinStatus());
        if (JOIN_STATUS_ACTIVE.equals(currentStatus)) {
            return ImportOutcome.ignored();
        }

        relation.setStudentNo(snapshotStudentNo);
        relation.setJoinStatus(JOIN_STATUS_ACTIVE);
        relation.setJoinedTime(now);
        relation.setLeftTime(null);
        relation.setUpdateBy(operatorId);
        relation.setUpdateTime(now);
        schoolClassStudentMapper.updateById(relation);
        return ImportOutcome.reactivated();
    }

    private StudentMatchResult findStudentById(Long tenantId, Long userId) {
        SysUserDO user = sysUserMapper.selectOne(Wrappers.<SysUserDO>lambdaQuery()
                .eq(SysUserDO::getTenantId, tenantId)
                .eq(SysUserDO::getId, userId)
                .eq(SysUserDO::getUserType, "student")
                .eq(SysUserDO::getStatus, ACTIVE_STATUS)
                .last("limit 1"));
        return user == null
                ? StudentMatchResult.error("用户ID未匹配到学生")
                : StudentMatchResult.success(user);
    }

    private StudentMatchResult findStudentByUserNo(Long tenantId, String userNo) {
        return findUniqueStudent("学号未匹配到学生", "学号匹配到了多个学生",
                Wrappers.<SysUserDO>lambdaQuery()
                        .eq(SysUserDO::getTenantId, tenantId)
                        .eq(SysUserDO::getUserType, "student")
                        .eq(SysUserDO::getStatus, ACTIVE_STATUS)
                        .eq(SysUserDO::getUserNo, userNo)
                        .last("limit 2"));
    }

    private StudentMatchResult findStudentByUsername(Long tenantId, String username) {
        return findUniqueStudent("登录账号未匹配到学生", "登录账号匹配到了多个学生",
                Wrappers.<SysUserDO>lambdaQuery()
                        .eq(SysUserDO::getTenantId, tenantId)
                        .eq(SysUserDO::getUserType, "student")
                        .eq(SysUserDO::getStatus, ACTIVE_STATUS)
                        .eq(SysUserDO::getUsername, username)
                        .last("limit 2"));
    }

    private StudentMatchResult findStudentByPhone(Long tenantId, String phone) {
        return findUniqueStudent("手机号未匹配到学生", "手机号匹配到了多个学生",
                Wrappers.<SysUserDO>lambdaQuery()
                        .eq(SysUserDO::getTenantId, tenantId)
                        .eq(SysUserDO::getUserType, "student")
                        .eq(SysUserDO::getStatus, ACTIVE_STATUS)
                        .eq(SysUserDO::getPhone, phone)
                        .last("limit 2"));
    }

    private StudentMatchResult findUniqueStudent(String notFoundMessage,
                                                 String duplicateMessage,
                                                 com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserDO> queryWrapper) {
        List<SysUserDO> users = sysUserMapper.selectList(queryWrapper);
        if (users.isEmpty()) {
            return StudentMatchResult.error(notFoundMessage);
        }
        if (users.size() > 1) {
            return StudentMatchResult.error(duplicateMessage);
        }
        return StudentMatchResult.success(users.get(0));
    }

    private SchoolAdminClassViews.StudentImportFailure buildFailure(int rowNo,
                                                                    ImportRow row,
                                                                    SysUserDO matchedUser,
                                                                    String reason) {
        return new SchoolAdminClassViews.StudentImportFailure(
                rowNo,
                parseLongValue(row.userIdText()),
                row.studentNo(),
                row.username(),
                row.phone(),
                row.name(),
                matchedUser == null ? null : matchedUser.getId(),
                resolveDisplayName(matchedUser),
                reason);
    }

    private String readCellText(Row row, Integer columnIndex, DataFormatter formatter) {
        if (columnIndex == null || columnIndex < 0) {
            return null;
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        String text = formatter.formatCellValue(cell);
        return StringUtils.hasText(text) ? text.trim() : null;
    }

    private long normalizePageNo(long pageNo) {
        return pageNo <= 0 ? 1 : pageNo;
    }

    private long normalizePageSize(long pageSize) {
        if (pageSize <= 0) {
            return 10;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private String normalizeText(String text) {
        return StringUtils.hasText(text) ? text.trim() : null;
    }

    private String normalizeCode(String text) {
        return StringUtils.hasText(text) ? text.trim().toUpperCase(Locale.ROOT) : null;
    }

    private String normalizeHeader(String text) {
        return normalizeCode(text).replace(" ", "");
    }

    private boolean containsIgnoreCase(String text, String keyword) {
        if (!StringUtils.hasText(text) || !StringUtils.hasText(keyword)) {
            return false;
        }
        return text.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private String resolveDisplayName(SysUserDO user) {
        if (user == null) {
            return null;
        }
        return firstNonBlank(user.getRealName(), user.getNickname(), user.getUsername());
    }

    private String buildResourceSpacePath(Long resourceSpaceId) {
        return resourceSpaceId == null ? null : "/school-admin/storage/class-res/" + resourceSpaceId;
    }

    private String normalizeTeacherRoleCode(String roleCode) {
        String normalized = normalizeCode(roleCode);
        if ("SUBJECT".equals(normalized)) {
            return "LECTURER";
        }
        return normalized;
    }

    private String normalizeCourseRelationType(String relationType) {
        String normalized = normalizeCode(relationType);
        if ("SECONDARY".equals(normalized)) {
            return "OPTIONAL";
        }
        return normalized;
    }

    private Long parseLongValue(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        String trimmed = text.trim();
        try {
            if (trimmed.matches("\\d+\\.0+")) {
                trimmed = trimmed.substring(0, trimmed.indexOf('.'));
            }
            return Long.parseLong(trimmed);
        } catch (NumberFormatException ex) {
            try {
                return new BigDecimal(trimmed).longValueExact();
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private long safeCount(Long count) {
        return count == null ? 0 : count;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private record ImportRow(String userIdText,
                             String studentNo,
                             String username,
                             String phone,
                             String name) {

        private boolean isEmpty() {
            return !StringUtils.hasText(userIdText)
                    && !StringUtils.hasText(studentNo)
                    && !StringUtils.hasText(username)
                    && !StringUtils.hasText(phone)
                    && !StringUtils.hasText(name);
        }

        private boolean hasAnyIdentifier() {
            return StringUtils.hasText(userIdText)
                    || StringUtils.hasText(studentNo)
                    || StringUtils.hasText(username)
                    || StringUtils.hasText(phone);
        }
    }

    private record StudentMatchResult(SysUserDO user, String message) {

        private static StudentMatchResult success(SysUserDO user) {
            return new StudentMatchResult(user, null);
        }

        private static StudentMatchResult error(String message) {
            return new StudentMatchResult(null, message);
        }

        private boolean success() {
            return user != null;
        }
    }

    private record ImportOutcome(ImportStatus status, SchoolAdminClassViews.StudentImportFailure failure) {

        private static ImportOutcome added() {
            return new ImportOutcome(ImportStatus.ADDED, null);
        }

        private static ImportOutcome reactivated() {
            return new ImportOutcome(ImportStatus.REACTIVATED, null);
        }

        private static ImportOutcome ignored() {
            return new ImportOutcome(ImportStatus.IGNORED, null);
        }

        private static ImportOutcome failure(SchoolAdminClassViews.StudentImportFailure failure) {
            return new ImportOutcome(ImportStatus.FAILED, failure);
        }
    }

    private enum ImportStatus {
        ADDED,
        REACTIVATED,
        IGNORED,
        FAILED
    }
}
