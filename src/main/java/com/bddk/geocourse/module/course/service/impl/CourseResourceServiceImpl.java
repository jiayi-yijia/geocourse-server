package com.bddk.geocourse.module.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.framework.tenant.TenantContextHolder;
import com.bddk.geocourse.module.course.dal.dataobject.CourseInfoDO;
import com.bddk.geocourse.module.course.dal.dataobject.CourseResourceDO;
import com.bddk.geocourse.module.course.dal.mapper.CourseInfoMapper;
import com.bddk.geocourse.module.course.dal.mapper.CourseResourceMapper;
import com.bddk.geocourse.module.course.model.CourseMetadata;
import com.bddk.geocourse.module.course.model.CourseChapterResourceView;
import com.bddk.geocourse.module.course.model.CourseChapterView;
import com.bddk.geocourse.module.course.model.CourseResourceCategory;
import com.bddk.geocourse.module.course.model.CourseResourceCreateCommand;
import com.bddk.geocourse.module.course.model.CourseResourceStorageMetadata;
import com.bddk.geocourse.module.course.model.CourseResourceUpdateRequest;
import com.bddk.geocourse.module.course.model.CourseResourceView;
import com.bddk.geocourse.module.course.service.CourseFileStorageService;
import com.bddk.geocourse.module.course.service.CourseResourceService;
import com.bddk.geocourse.module.course.service.StoredResource;
import com.bddk.geocourse.module.course.service.support.CourseViewMapper;
import com.bddk.geocourse.module.identity.stp.StpAdminUtil;
import com.bddk.geocourse.module.identity.stp.StpSchoolAdminUtil;
import com.bddk.geocourse.module.identity.stp.StpTeacherUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseResourceServiceImpl implements CourseResourceService {

    private static final Map<String, Integer> RESOURCE_SORT = Map.ofEntries(
            Map.entry("video", 10),
            Map.entry("handout", 20),
            Map.entry("material", 30),
            Map.entry("exercise", 40),
            Map.entry("audio", 50),
            Map.entry("image", 60),
            Map.entry("questionBank", 70),
            Map.entry("answerAnalysis", 80),
            Map.entry("outline", 90),
            Map.entry("teachingCase", 100),
            Map.entry("implementationPlan", 110),
            Map.entry("expertGuideVideo", 120)
    );

    private static final Set<String> VIDEO_RESOURCE_TYPES = Set.of("video", "expertGuideVideo");

    private final CourseInfoMapper courseInfoMapper;
    private final CourseResourceMapper courseResourceMapper;
    private final CourseFileStorageService courseFileStorageService;
    private final CourseViewMapper courseViewMapper;
    private final ObjectMapper objectMapper;

    public CourseResourceServiceImpl(CourseInfoMapper courseInfoMapper,
                                     CourseResourceMapper courseResourceMapper,
                                     CourseFileStorageService courseFileStorageService,
                                     CourseViewMapper courseViewMapper,
                                     ObjectMapper objectMapper) {
        this.courseInfoMapper = courseInfoMapper;
        this.courseResourceMapper = courseResourceMapper;
        this.courseFileStorageService = courseFileStorageService;
        this.courseViewMapper = courseViewMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<CourseResourceView> listByCategory(String category) {
        Long tenantId = requiredTenantId();
        CourseResourceCategory targetCategory = CourseResourceCategory.fromPath(category);
        List<CourseInfoDO> courseInfos = courseInfoMapper.selectList(new LambdaQueryWrapper<CourseInfoDO>()
                .eq(CourseInfoDO::getTenantId, tenantId)
                .eq(CourseInfoDO::getStatus, 1)
                .orderByDesc(CourseInfoDO::getCreateTime)
                .orderByDesc(CourseInfoDO::getId));
        List<CourseInfoDO> matchedInfos = courseInfos.stream()
                .filter(item -> courseViewMapper.resolveCategory(item).pathValue().equals(targetCategory.pathValue()))
                .toList();
        if (matchedInfos.isEmpty()) {
            return List.of();
        }

        List<Long> courseIds = matchedInfos.stream().map(CourseInfoDO::getId).toList();
        List<CourseResourceDO> resourceList = courseResourceMapper.selectList(new LambdaQueryWrapper<CourseResourceDO>()
                .eq(CourseResourceDO::getTenantId, tenantId)
                .in(CourseResourceDO::getCourseId, courseIds)
                .eq(CourseResourceDO::getStatus, 1)
                .orderByAsc(CourseResourceDO::getSortNo)
                .orderByAsc(CourseResourceDO::getId));
        Map<Long, List<CourseResourceDO>> resourceMap = resourceList.stream()
                .collect(Collectors.groupingBy(CourseResourceDO::getCourseId, LinkedHashMap::new, Collectors.toList()));

        return matchedInfos.stream()
                .map(info -> courseViewMapper.toView(info, resourceMap.getOrDefault(info.getId(), List.of())))
                .toList();
    }

    @Override
    public CourseResourceView getCourseDetail(Long courseId) {
        Long tenantId = requiredTenantId();
        CourseInfoDO courseInfo = getCourseInfoOrThrow(tenantId, courseId);
        List<CourseResourceDO> resourceList = courseResourceMapper.selectList(new LambdaQueryWrapper<CourseResourceDO>()
                .eq(CourseResourceDO::getTenantId, tenantId)
                .eq(CourseResourceDO::getCourseId, courseId)
                .eq(CourseResourceDO::getStatus, 1)
                .orderByAsc(CourseResourceDO::getChapterId)
                .orderByAsc(CourseResourceDO::getSortNo)
                .orderByAsc(CourseResourceDO::getId));

        CourseResourceView view = courseViewMapper.toView(courseInfo, resourceList);
        List<CourseChapterView> chapters = buildChapterViews(resourceList);
        view.setChapters(chapters);
        view.setChapterCount(chapters.size());
        return view;
    }

    @Override
    @Transactional
    public CourseResourceView create(String category, CourseResourceCreateCommand command) {
        requireWritePermission();
        Long tenantId = requiredTenantId();
        CourseResourceCategory targetCategory = CourseResourceCategory.fromPath(category);

        if (command.getVideoFile() == null || command.getVideoFile().isEmpty()) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "请上传视频文件");
        }
        CourseMetadata metadata = new CourseMetadata();
        metadata.setCategory(targetCategory.pathValue());
        metadata.setModule(trimToNull(command.getModule()));
        metadata.setStage(trimToNull(command.getStage()));
        metadata.setDifficulty(trimToNull(command.getDifficulty()));
        metadata.setClassHours(command.getClassHours());
        metadata.setPhase(trimToNull(command.getPhase()));
        metadata.setTopicType(trimToNull(command.getTopicType()));
        metadata.setEncrypted(Boolean.TRUE.equals(command.getEncrypted()));
        metadata.setAccessScope(trimToNull(command.getAccessScope()));
        metadata.setDownloadPermission(trimToNull(command.getDownloadPermission()));

        StoredResource imageResource = storeResource(targetCategory, "image", command.getImageFile());

        CourseInfoDO courseInfo = new CourseInfoDO();
        courseInfo.setTenantId(tenantId);
        courseInfo.setCourseCode(generateCourseCode(targetCategory));
        courseInfo.setCourseName(trimToNull(command.getTitle()));
        courseInfo.setCourseType(targetCategory.courseTypeValue());
        courseInfo.setSubjectCode(courseViewMapper.resolveSubjectCode(command.getModule()));
        courseInfo.setCoverUrl(imageResource == null ? null : imageResource.url());
        courseInfo.setIntroText(trimToNull(command.getDescription()));
        courseInfo.setTeacherId(currentOperatorIdOrNull());
        courseInfo.setSaleType("free");
        courseInfo.setPriceAmount(BigDecimal.ZERO);
        courseInfo.setPublishStatus("published");
        courseInfo.setLearnMode(targetCategory == CourseResourceCategory.STUDENT ? "class" : "self");
        courseInfo.setStatus(1);
        courseInfo.setRemark(toMetadataJson(metadata));
        courseInfoMapper.insert(courseInfo);

        List<CourseResourceDO> resources = new ArrayList<>();
        resources.add(buildResourceDO(courseInfo.getId(), tenantId, targetCategory, "video", command.getVideoFile()));
        resources.add(buildOptionalResourceDO(courseInfo.getId(), tenantId, targetCategory, "handout", command.getHandoutFile()));
        resources.add(buildOptionalResourceDO(courseInfo.getId(), tenantId, targetCategory, "material", command.getMaterialFile()));
        resources.add(buildOptionalResourceDO(courseInfo.getId(), tenantId, targetCategory, "exercise", command.getExerciseFile()));
        resources.add(buildOptionalResourceDO(courseInfo.getId(), tenantId, targetCategory, "audio", command.getAudioFile()));
        resources.add(buildOptionalResourceDO(courseInfo.getId(), tenantId, "image", command.getImageFile(), imageResource));
        resources.add(buildOptionalResourceDO(courseInfo.getId(), tenantId, targetCategory, "questionBank", command.getQuestionBankFile()));
        resources.add(buildOptionalResourceDO(courseInfo.getId(), tenantId, targetCategory, "answerAnalysis", command.getAnswerAnalysisFile()));
        resources.add(buildOptionalResourceDO(courseInfo.getId(), tenantId, targetCategory, "outline", command.getOutlineFile()));
        resources.add(buildOptionalResourceDO(courseInfo.getId(), tenantId, targetCategory, "teachingCase", command.getTeachingCaseFile()));
        resources.add(buildOptionalResourceDO(courseInfo.getId(), tenantId, targetCategory, "implementationPlan", command.getImplementationPlanFile()));
        resources.add(buildOptionalResourceDO(courseInfo.getId(), tenantId, targetCategory, "expertGuideVideo", command.getExpertGuideVideoFile()));
        resources.stream().filter(Objects::nonNull).forEach(courseResourceMapper::insert);

        return courseViewMapper.toView(courseInfo, resources.stream().filter(Objects::nonNull).toList());
    }

    @Override
    @Transactional
    public CourseResourceView update(String category, Long courseId, CourseResourceUpdateRequest request) {
        requireWritePermission();
        Long tenantId = requiredTenantId();
        CourseInfoDO courseInfo = getCourseInfoOrThrow(tenantId, courseId);
        CourseResourceCategory targetCategory = CourseResourceCategory.fromPath(category);
        if (!courseViewMapper.resolveCategory(courseInfo).pathValue().equals(targetCategory.pathValue())) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "课程资源不存在");
        }

        CourseMetadata metadata = courseViewMapper.parseMetadata(courseInfo.getRemark());
        metadata.setCategory(targetCategory.pathValue());
        metadata.setModule(trimToNull(request.getModule()));
        metadata.setStage(trimToNull(request.getStage()));
        metadata.setDifficulty(trimToNull(request.getDifficulty()));
        metadata.setClassHours(request.getClassHours());

        courseInfo.setCourseName(trimToNull(request.getTitle()));
        courseInfo.setSubjectCode(courseViewMapper.resolveSubjectCode(request.getModule()));
        courseInfo.setIntroText(trimToNull(request.getDescription()));
        courseInfo.setRemark(toMetadataJson(metadata));
        courseInfoMapper.updateById(courseInfo);

        List<CourseResourceDO> resources = courseResourceMapper.selectList(new LambdaQueryWrapper<CourseResourceDO>()
                .eq(CourseResourceDO::getTenantId, tenantId)
                .eq(CourseResourceDO::getCourseId, courseId)
                .eq(CourseResourceDO::getStatus, 1)
                .orderByAsc(CourseResourceDO::getSortNo)
                .orderByAsc(CourseResourceDO::getId));
        return courseViewMapper.toView(courseInfo, resources);
    }

    @Override
    @Transactional
    public void delete(String category, Long courseId) {
        requireWritePermission();
        Long tenantId = requiredTenantId();
        CourseInfoDO courseInfo = getCourseInfoOrThrow(tenantId, courseId);
        CourseResourceCategory targetCategory = CourseResourceCategory.fromPath(category);
        if (!courseViewMapper.resolveCategory(courseInfo).pathValue().equals(targetCategory.pathValue())) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "课程资源不存在");
        }

        List<CourseResourceDO> resources = courseResourceMapper.selectList(new LambdaQueryWrapper<CourseResourceDO>()
                .eq(CourseResourceDO::getTenantId, tenantId)
                .eq(CourseResourceDO::getCourseId, courseId));
        resources.forEach(item -> {
            deleteStoredResource(item);
            courseResourceMapper.deleteById(item.getId());
        });
        if (courseInfo.getCoverUrl() != null) {
            courseFileStorageService.deleteByUrl(courseInfo.getCoverUrl());
        }
        courseInfoMapper.deleteById(courseId);
    }

    private CourseInfoDO getCourseInfoOrThrow(Long tenantId, Long courseId) {
        CourseInfoDO courseInfo = courseInfoMapper.selectOne(new LambdaQueryWrapper<CourseInfoDO>()
                .eq(CourseInfoDO::getTenantId, tenantId)
                .eq(CourseInfoDO::getId, courseId)
                .last("limit 1"));
        if (courseInfo == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "课程资源不存在");
        }
        return courseInfo;
    }

    private CourseResourceDO buildResourceDO(Long courseId,
                                             Long tenantId,
                                             CourseResourceCategory category,
                                             String resourceType,
                                             MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "缺少文件: " + resourceType);
        }
        return buildOptionalResourceDO(courseId, tenantId, category, resourceType, file);
    }

    private CourseResourceDO buildOptionalResourceDO(Long courseId,
                                                     Long tenantId,
                                                     CourseResourceCategory category,
                                                     String resourceType,
                                                     MultipartFile file) {
        return buildOptionalResourceDO(courseId, tenantId, resourceType, file, storeResource(category, resourceType, file));
    }

    private CourseResourceDO buildOptionalResourceDO(Long courseId,
                                                     Long tenantId,
                                                     String resourceType,
                                                     MultipartFile file,
                                                     StoredResource storedResource) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (storedResource == null) {
            throw new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "文件上传失败");
        }
        CourseResourceDO resource = new CourseResourceDO();
        resource.setTenantId(tenantId);
        resource.setCourseId(courseId);
        resource.setResourceType(resourceType);
        resource.setResourceName(file.getOriginalFilename());
        resource.setResourceUrl(storedResource.url());
        resource.setSortNo(RESOURCE_SORT.getOrDefault(resourceType, 999));
        resource.setStatus(1);
        resource.setRemark(toResourceStorageJson(storedResource));
        return resource;
    }

    private StoredResource storeResource(CourseResourceCategory category, String resourceType, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return courseFileStorageService.store(category.pathValue(), resourceType, file);
    }

    private void deleteStoredResource(CourseResourceDO resource) {
        CourseResourceStorageMetadata storageMetadata = parseResourceStorageMetadata(resource.getRemark());
        if (storageMetadata != null && "minio".equalsIgnoreCase(storageMetadata.getProvider())) {
            courseFileStorageService.deleteByObjectKey(storageMetadata.getObjectKey());
            return;
        }
        courseFileStorageService.deleteByUrl(resource.getResourceUrl());
    }

    private CourseResourceView toView(CourseInfoDO info, List<CourseResourceDO> resourceList) {
        CourseMetadata metadata = parseMetadata(info.getRemark());
        Map<String, String> resourceUrls = resourceList.stream()
                .filter(item -> item.getResourceType() != null && item.getResourceUrl() != null)
                .collect(Collectors.toMap(
                        CourseResourceDO::getResourceType,
                        CourseResourceDO::getResourceUrl,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        CourseResourceCategory category = resolveCategory(info);
        CourseResourceView view = new CourseResourceView();
        view.setId(info.getId());
        view.setCategory(category.pathValue());
        view.setTitle(info.getCourseName());
        view.setModule(defaultValue(metadata.getModule(), resolveModule(info.getSubjectCode())));
        view.setStage(defaultValue(metadata.getStage(), resolveStage(info.getCourseName())));
        view.setDifficulty(defaultValue(metadata.getDifficulty(), "基础"));
        view.setClassHours(metadata.getClassHours() == null ? 1 : metadata.getClassHours());
        view.setVideoUrl(resourceUrls.get("video"));
        view.setHandoutUrl(resourceUrls.get("handout"));
        view.setMaterialUrl(resourceUrls.get("material"));
        view.setExerciseUrl(resourceUrls.get("exercise"));
        view.setAudioUrl(resourceUrls.get("audio"));
        view.setImageUrl(defaultValue(resourceUrls.get("image"), info.getCoverUrl()));
        view.setQuestionBankUrl(resourceUrls.get("questionBank"));
        view.setAnswerAnalysisUrl(resourceUrls.get("answerAnalysis"));
        view.setOutlineUrl(resourceUrls.get("outline"));
        view.setTeachingCaseUrl(resourceUrls.get("teachingCase"));
        view.setImplementationPlanUrl(resourceUrls.get("implementationPlan"));
        view.setExpertGuideVideoUrl(resourceUrls.get("expertGuideVideo"));
        view.setPhase(metadata.getPhase());
        view.setTopicType(metadata.getTopicType());
        view.setEncrypted(Boolean.TRUE.equals(metadata.getEncrypted()));
        view.setAccessScope(defaultValue(metadata.getAccessScope(), "校内"));
        view.setDownloadPermission(defaultValue(metadata.getDownloadPermission(), "教师可下载"));
        view.setDescription(info.getIntroText());
        view.setCreatedAt(info.getCreateTime());
        return view;
    }

    private CourseMetadata parseMetadata(String raw) {
        if (raw == null || raw.isBlank()) {
            return new CourseMetadata();
        }
        try {
            return objectMapper.readValue(raw, CourseMetadata.class);
        } catch (JsonProcessingException ex) {
            return new CourseMetadata();
        }
    }

    private String toMetadataJson(CourseMetadata metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException ex) {
            throw new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "课程元数据序列化失败");
        }
    }

    private CourseResourceStorageMetadata parseResourceStorageMetadata(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(raw, CourseResourceStorageMetadata.class);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private List<CourseChapterView> buildChapterViews(List<CourseResourceDO> resourceList) {
        if (resourceList == null || resourceList.isEmpty()) {
            return List.of();
        }

        List<CourseResourceDO> unassignedResources = new ArrayList<>();
        Map<Long, List<CourseResourceDO>> resourcesByChapterId = new LinkedHashMap<>();
        for (CourseResourceDO resource : resourceList) {
            if (resource.getChapterId() == null) {
                unassignedResources.add(resource);
                continue;
            }
            resourcesByChapterId.computeIfAbsent(resource.getChapterId(), key -> new ArrayList<>()).add(resource);
        }

        List<CourseChapterView> chapters = new ArrayList<>();
        int order = 1;
        if (!unassignedResources.isEmpty()) {
            chapters.add(toChapterView(null, order++, unassignedResources));
        }

        for (Map.Entry<Long, List<CourseResourceDO>> entry : resourcesByChapterId.entrySet()) {
            chapters.add(toChapterView(entry.getKey(), order++, entry.getValue()));
        }

        if (chapters.isEmpty()) {
            chapters.add(toChapterView(null, 1, resourceList));
        }
        return chapters;
    }

    private CourseChapterView toChapterView(Long chapterId, int sortNo, List<CourseResourceDO> resources) {
        List<CourseChapterResourceView> resourceViews = resources.stream()
                .map(this::toChapterResourceView)
                .toList();
        CourseChapterResourceView primaryVideo = resourceViews.stream()
                .filter(item -> VIDEO_RESOURCE_TYPES.contains(item.getResourceType()))
                .findFirst()
                .orElse(null);

        CourseChapterView chapterView = new CourseChapterView();
        chapterView.setChapterId(chapterId);
        chapterView.setSortNo(sortNo);
        chapterView.setTitle(resolveChapterTitle(sortNo, resourceViews));
        chapterView.setResourceCount(resourceViews.size());
        chapterView.setVideoCount((int) resourceViews.stream()
                .filter(item -> VIDEO_RESOURCE_TYPES.contains(item.getResourceType()))
                .count());
        chapterView.setPrimaryVideoUrl(primaryVideo == null ? null : primaryVideo.getUrl());
        chapterView.setPrimaryVideoCoverUrl(primaryVideo == null ? null : primaryVideo.getCoverUrl());
        chapterView.setResources(resourceViews);
        return chapterView;
    }

    private CourseChapterResourceView toChapterResourceView(CourseResourceDO resource) {
        CourseResourceStorageMetadata metadata = parseResourceStorageMetadata(resource.getRemark());
        CourseChapterResourceView view = new CourseChapterResourceView();
        view.setResourceId(resource.getId());
        view.setChapterId(resource.getChapterId());
        view.setResourceType(resource.getResourceType());
        view.setTitle(resolveResourceTitle(resource, metadata));
        view.setFileName(resolveFileName(resource, metadata));
        view.setUrl(resource.getResourceUrl());
        view.setCoverUrl(metadata == null ? null : metadata.getCoverUrl());
        view.setFileSize(metadata == null ? null : metadata.getFileSize());
        view.setContentType(metadata == null ? null : metadata.getContentType());
        view.setSortNo(resource.getSortNo());
        view.setCreatedAt(resource.getCreateTime());
        return view;
    }

    private String resolveChapterTitle(int order, List<CourseChapterResourceView> resources) {
        String representativeTitle = resources.stream()
                .filter(item -> item.getTitle() != null && !item.getTitle().isBlank())
                .sorted((left, right) -> {
                    boolean leftVideo = VIDEO_RESOURCE_TYPES.contains(left.getResourceType());
                    boolean rightVideo = VIDEO_RESOURCE_TYPES.contains(right.getResourceType());
                    if (leftVideo == rightVideo) {
                        return Integer.compare(
                                left.getSortNo() == null ? Integer.MAX_VALUE : left.getSortNo(),
                                right.getSortNo() == null ? Integer.MAX_VALUE : right.getSortNo()
                        );
                    }
                    return leftVideo ? -1 : 1;
                })
                .map(CourseChapterResourceView::getTitle)
                .map(this::stripExtension)
                .findFirst()
                .orElse(null);

        String prefix = "\u7b2c" + order + "\u7ae0";
        if (representativeTitle == null || representativeTitle.isBlank()) {
            return prefix + " \u00b7 \u8bfe\u7a0b\u5bfc\u5b66";
        }
        return prefix + " \u00b7 " + representativeTitle;
    }

    private String resolveResourceTitle(CourseResourceDO resource, CourseResourceStorageMetadata metadata) {
        if (metadata != null && metadata.getResourceTitle() != null && !metadata.getResourceTitle().isBlank()) {
            return metadata.getResourceTitle().trim();
        }
        String fileName = resolveFileName(resource, metadata);
        return stripExtension(fileName);
    }

    private String resolveFileName(CourseResourceDO resource, CourseResourceStorageMetadata metadata) {
        if (metadata != null && metadata.getOriginalFileName() != null && !metadata.getOriginalFileName().isBlank()) {
            return metadata.getOriginalFileName().trim();
        }
        return resource.getResourceName();
    }

    private String stripExtension(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        int lastDotIndex = value.lastIndexOf('.');
        if (lastDotIndex <= 0) {
            return value;
        }
        return value.substring(0, lastDotIndex);
    }

    private String toResourceStorageJson(StoredResource storedResource) {
        CourseResourceStorageMetadata metadata = new CourseResourceStorageMetadata();
        metadata.setProvider(storedResource.provider());
        metadata.setObjectKey(storedResource.objectKey());
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException ex) {
            throw new ServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "资源存储元数据序列化失败");
        }
    }

    private CourseResourceCategory resolveCategory(CourseInfoDO info) {
        CourseMetadata metadata = parseMetadata(info.getRemark());
        if (metadata.getCategory() != null && !metadata.getCategory().isBlank()) {
            return CourseResourceCategory.fromPath(metadata.getCategory());
        }
        String courseType = defaultValue(info.getCourseType(), "");
        return switch (courseType.toLowerCase()) {
            case "teacher" -> CourseResourceCategory.TEACHER;
            case "interdisciplinary", "public" -> CourseResourceCategory.INTERDISCIPLINARY;
            default -> CourseResourceCategory.STUDENT;
        };
    }

    private String resolveSubjectCode(String module) {
        if (module == null) {
            return "geoscience";
        }
        return switch (module.trim()) {
            case "构造地质" -> "tectonics";
            case "岩石学" -> "petrology";
            case "地球化学" -> "geochemistry";
            case "古生物" -> "paleontology";
            case "地球物理" -> "geophysics";
            case "大气与海洋" -> "atmosphere-ocean";
            case "天文与空间" -> "astronomy-space";
            case "遥感" -> "remote-sensing";
            case "环境" -> "environment";
            default -> "geoscience";
        };
    }

    private String resolveModule(String subjectCode) {
        if (subjectCode == null || subjectCode.isBlank()) {
            return "地球科学";
        }
        return switch (subjectCode) {
            case "tectonics" -> "构造地质";
            case "petrology" -> "岩石学";
            case "geochemistry" -> "地球化学";
            case "paleontology" -> "古生物";
            case "geophysics" -> "地球物理";
            case "atmosphere-ocean" -> "大气与海洋";
            case "astronomy-space" -> "天文与空间";
            case "remote-sensing" -> "遥感";
            case "environment" -> "环境";
            case "chinese" -> "语文";
            case "math" -> "数学";
            case "english" -> "英语";
            default -> "地球科学";
        };
    }

    private String resolveStage(String courseName) {
        String text = defaultValue(courseName, "");
        if (text.contains("小学")) {
            return "小学";
        }
        if (text.contains("七年级") || text.contains("八年级") || text.contains("九年级") || text.contains("初")) {
            return "初中";
        }
        if (text.contains("高")) {
            return "高中";
        }
        if (text.contains("大学")) {
            return "大学";
        }
        return "高中";
    }

    private String generateCourseCode(CourseResourceCategory category) {
        return "COURSE-" + category.pathValue().toUpperCase() + "-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    private void requireWritePermission() {
        if (StpAdminUtil.stpLogic.isLogin()
                || StpSchoolAdminUtil.stpLogic.isLogin()
                || StpTeacherUtil.stpLogic.isLogin()) {
            return;
        }
        throw new ServiceException(ErrorCode.UNAUTHORIZED);
    }

    private Long currentOperatorIdOrNull() {
        try {
            if (StpAdminUtil.stpLogic.isLogin()) {
                return StpAdminUtil.stpLogic.getLoginIdAsLong();
            }
            if (StpSchoolAdminUtil.stpLogic.isLogin()) {
                return StpSchoolAdminUtil.stpLogic.getLoginIdAsLong();
            }
            if (StpTeacherUtil.stpLogic.isLogin()) {
                return StpTeacherUtil.stpLogic.getLoginIdAsLong();
            }
        } catch (Exception ignored) {
            // ignore
        }
        return null;
    }

    private Long requiredTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new ServiceException(ErrorCode.TENANT_REQUIRED);
        }
        return tenantId;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
