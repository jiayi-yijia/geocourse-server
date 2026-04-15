package com.bddk.geocourse.module.compat.service;

import com.bddk.geocourse.framework.tenant.TenantContextHolder;
import com.bddk.geocourse.module.compat.model.SchoolPortalAdminPayload;
import com.bddk.geocourse.module.compat.model.SchoolPortalContentItemView;
import com.bddk.geocourse.module.compat.model.SchoolPortalContentOptionsResponse;
import com.bddk.geocourse.module.compat.model.SchoolPortalPublicResponse;
import com.bddk.geocourse.module.course.model.CourseResourceView;
import com.bddk.geocourse.module.course.service.CourseResourceService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class FrontendSchoolPortalService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CourseResourceService courseResourceService;
    private final FrontendRosterService frontendRosterService;
    private final ObjectMapper objectMapper;
    private final Map<Long, SchoolPortalAdminPayload> configByTenant = new LinkedHashMap<>();
    private final Map<String, Long> tenantBySlug = new LinkedHashMap<>();
    private final Path storagePath = Path.of(System.getProperty("java.io.tmpdir"), "geocourse-school-portal-config.json");

    public FrontendSchoolPortalService(CourseResourceService courseResourceService,
                                       FrontendRosterService frontendRosterService,
                                       ObjectMapper objectMapper) {
        this.courseResourceService = courseResourceService;
        this.frontendRosterService = frontendRosterService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public synchronized void loadPersistedConfigs() {
        if (!Files.exists(storagePath)) {
            return;
        }
        try {
            List<PortalConfigEntry> entries = objectMapper.readValue(
                    Files.readString(storagePath),
                    new TypeReference<List<PortalConfigEntry>>() {
                    }
            );
            configByTenant.clear();
            tenantBySlug.clear();
            for (PortalConfigEntry entry : entries) {
                if (entry == null || entry.payload() == null) {
                    continue;
                }
                configByTenant.put(entry.tenantId(), entry.payload());
                tenantBySlug.put(entry.payload().slug(), entry.tenantId());
            }
        } catch (IOException ignored) {
            configByTenant.clear();
            tenantBySlug.clear();
        }
    }

    public synchronized SchoolPortalAdminPayload getAdminConfig(Long tenantId, String fallbackSchoolName) {
        SchoolPortalAdminPayload existing = configByTenant.get(tenantId);
        if (existing != null) {
            return existing;
        }
        String schoolName = defaultSchoolName(tenantId, fallbackSchoolName);
        String slug = defaultSlug(schoolName, tenantId);
        SchoolPortalAdminPayload created = new SchoolPortalAdminPayload(
                slug,
                schoolName,
                "Welcome to " + schoolName,
                "Explore courses, question banks, and school updates in one place.",
                "",
                "#16a34a",
                true,
                true,
                true,
                true,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
        configByTenant.put(tenantId, created);
        tenantBySlug.put(slug, tenantId);
        persistConfigs();
        return created;
    }

    public synchronized SchoolPortalAdminPayload saveAdminConfig(Long tenantId, SchoolPortalAdminPayload payload) {
        SchoolPortalAdminPayload current = getAdminConfig(tenantId, payload.schoolName());
        String slug = normalizeSlug(payload.slug(), tenantId);
        Long existingTenant = tenantBySlug.get(slug);
        if (existingTenant != null && !Objects.equals(existingTenant, tenantId)) {
            throw new IllegalArgumentException("Slug already exists");
        }
        if (!Objects.equals(current.slug(), slug)) {
            tenantBySlug.remove(current.slug());
        }
        SchoolPortalAdminPayload saved = new SchoolPortalAdminPayload(
                slug,
                defaultSchoolName(tenantId, payload.schoolName()),
                defaultText(payload.heroTitle(), "Welcome to " + defaultSchoolName(tenantId, payload.schoolName())),
                defaultText(payload.heroSubtitle(), "Explore courses, question banks, and school updates in one place."),
                payload.footerNote(),
                defaultText(payload.accentColor(), "#16a34a"),
                payload.showOpenCourses(),
                payload.showCourses(),
                payload.showQuestionBank(),
                payload.showQa(),
                safeLongList(payload.selectedOpenCourseIds()),
                safeLongList(payload.selectedCourseIds()),
                safeLongList(payload.selectedQuestionBankCourseIds()),
                safeStringList(payload.qaItems())
        );
        configByTenant.put(tenantId, saved);
        tenantBySlug.put(saved.slug(), tenantId);
        persistConfigs();
        return saved;
    }

    public SchoolPortalContentOptionsResponse getContentOptions(Long tenantId) {
        ContentBuckets buckets = buildContentBuckets(tenantId);
        return new SchoolPortalContentOptionsResponse(buckets.openCourses(), buckets.courses(), buckets.questionBanks());
    }

    public SchoolPortalPublicResponse getMyPortal(Long tenantId, String fallbackSchoolName) {
        return toPublicResponse(tenantId, getAdminConfig(tenantId, fallbackSchoolName));
    }

    public synchronized SchoolPortalPublicResponse findPublicBySlug(String slug) {
        if (!StringUtils.hasText(slug)) {
            return null;
        }
        Long tenantId = tenantBySlug.get(slug.trim().toLowerCase());
        if (tenantId == null) {
            return null;
        }
        SchoolPortalAdminPayload payload = configByTenant.get(tenantId);
        return payload == null ? null : toPublicResponse(tenantId, payload);
    }

    public synchronized String findConfiguredSchoolName(Long tenantId) {
        SchoolPortalAdminPayload payload = configByTenant.get(tenantId);
        if (payload == null || !StringUtils.hasText(payload.schoolName())) {
            return null;
        }
        return payload.schoolName().trim();
    }

    private SchoolPortalPublicResponse toPublicResponse(Long tenantId, SchoolPortalAdminPayload payload) {
        ContentBuckets buckets = buildContentBuckets(tenantId);
        return new SchoolPortalPublicResponse(
                payload.slug(),
                payload.schoolName(),
                payload.heroTitle(),
                payload.heroSubtitle(),
                payload.footerNote(),
                payload.accentColor(),
                payload.showOpenCourses(),
                payload.showCourses(),
                payload.showQuestionBank(),
                payload.showQa(),
                safeStringList(payload.qaItems()),
                filterSelection(buckets.openCourses(), payload.selectedOpenCourseIds()),
                filterSelection(buckets.courses(), payload.selectedCourseIds()),
                filterSelection(buckets.questionBanks(), payload.selectedQuestionBankCourseIds()),
                frontendRosterService.countTeachers(tenantId),
                frontendRosterService.countStudents(tenantId)
        );
    }

    private ContentBuckets buildContentBuckets(Long tenantId) {
        Long previousTenantId = TenantContextHolder.getTenantId();
        try {
            TenantContextHolder.setTenantId(tenantId);
            List<SchoolPortalContentItemView> teacherCourses = mapCourses(courseResourceService.listByCategory("teacher"));
            List<SchoolPortalContentItemView> studentCourses = mapCourses(courseResourceService.listByCategory("student"));
            List<SchoolPortalContentItemView> interdisciplinaryCourses = mapCourses(courseResourceService.listByCategory("interdisciplinary"));

            List<SchoolPortalContentItemView> courses = teacherCourses.isEmpty() ? new ArrayList<>(studentCourses) : new ArrayList<>(teacherCourses);
            List<SchoolPortalContentItemView> openCourses = new ArrayList<>(interdisciplinaryCourses);
            teacherCourses.stream().filter(item -> isOpenScope(item.accessScope())).forEach(openCourses::add);
            List<SchoolPortalContentItemView> questionBanks = new ArrayList<>();
            teacherCourses.stream().filter(SchoolPortalContentItemView::hasQuestionBank).forEach(questionBanks::add);
            interdisciplinaryCourses.stream().filter(SchoolPortalContentItemView::hasQuestionBank).forEach(questionBanks::add);
            return new ContentBuckets(deduplicate(openCourses), deduplicate(courses), deduplicate(questionBanks));
        } finally {
            if (previousTenantId == null) {
                TenantContextHolder.clear();
            } else {
                TenantContextHolder.setTenantId(previousTenantId);
            }
        }
    }

    private List<SchoolPortalContentItemView> mapCourses(List<CourseResourceView> courseViews) {
        return courseViews.stream()
                .map(item -> new SchoolPortalContentItemView(
                        item.getId(),
                        item.getTitle(),
                        item.getModule(),
                        item.getAccessScope(),
                        item.getCreatedAt() == null ? null : DATETIME_FORMATTER.format(item.getCreatedAt()),
                        null,
                        null,
                        item.getImageUrl(),
                        StringUtils.hasText(item.getQuestionBankUrl())
                ))
                .toList();
    }

    private List<SchoolPortalContentItemView> filterSelection(List<SchoolPortalContentItemView> items, List<Long> selectedIds) {
        if (selectedIds == null || selectedIds.isEmpty()) {
            return items;
        }
        return items.stream().filter(item -> selectedIds.contains(item.id())).toList();
    }

    private List<SchoolPortalContentItemView> deduplicate(List<SchoolPortalContentItemView> items) {
        Map<Long, SchoolPortalContentItemView> rows = new LinkedHashMap<>();
        for (SchoolPortalContentItemView item : items) {
            if (item.id() != null && !rows.containsKey(item.id())) {
                rows.put(item.id(), item);
            }
        }
        return new ArrayList<>(rows.values());
    }

    private boolean isOpenScope(String accessScope) {
        if (!StringUtils.hasText(accessScope)) {
            return false;
        }
        String value = accessScope.trim().toLowerCase();
        return value.contains("open") || value.contains("public") || accessScope.contains("公开");
    }

    private String defaultSchoolName(Long tenantId, String fallbackSchoolName) {
        if (StringUtils.hasText(fallbackSchoolName)) {
            return fallbackSchoolName.trim();
        }
        String configured = findConfiguredSchoolName(tenantId);
        return StringUtils.hasText(configured) ? configured : "School " + tenantId;
    }

    private String normalizeSlug(String rawSlug, Long tenantId) {
        String base = rawSlug == null ? "" : rawSlug.trim().toLowerCase();
        base = base.replaceAll("[^a-z0-9-]", "-").replaceAll("-{2,}", "-");
        base = base.replaceAll("^-+", "").replaceAll("-+$", "");
        return StringUtils.hasText(base) ? base : defaultSlug(null, tenantId);
    }

    private String defaultSlug(String schoolName, Long tenantId) {
        if (StringUtils.hasText(schoolName)) {
            String candidate = schoolName.trim().toLowerCase()
                    .replaceAll("[^a-z0-9-]", "-")
                    .replaceAll("-{2,}", "-")
                    .replaceAll("^-+", "")
                    .replaceAll("-+$", "");
            if (StringUtils.hasText(candidate)) {
                return candidate;
            }
        }
        return "school-" + tenantId;
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private List<Long> safeLongList(List<Long> values) {
        return values == null ? List.of() : values.stream().filter(Objects::nonNull).toList();
    }

    private List<String> safeStringList(List<String> values) {
        return values == null ? List.of() : values.stream().filter(StringUtils::hasText).map(String::trim).toList();
    }

    private void persistConfigs() {
        List<PortalConfigEntry> entries = configByTenant.entrySet().stream()
                .map(entry -> new PortalConfigEntry(entry.getKey(), entry.getValue()))
                .toList();
        try {
            Files.writeString(storagePath, objectMapper.writeValueAsString(entries));
        } catch (IOException ignored) {
            // Keep runtime behavior available even if local persistence fails.
        }
    }

    private record ContentBuckets(
            List<SchoolPortalContentItemView> openCourses,
            List<SchoolPortalContentItemView> courses,
            List<SchoolPortalContentItemView> questionBanks
    ) {
    }

    private record PortalConfigEntry(Long tenantId, SchoolPortalAdminPayload payload) {
    }
}
