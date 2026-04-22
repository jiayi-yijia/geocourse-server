package com.bddk.geocourse.module.course.service.support;

import com.bddk.geocourse.module.course.dal.dataobject.CourseInfoDO;
import com.bddk.geocourse.module.course.dal.dataobject.CourseResourceDO;
import com.bddk.geocourse.module.course.model.CourseMetadata;
import com.bddk.geocourse.module.course.model.CourseResourceCategory;
import com.bddk.geocourse.module.course.model.CourseResourceView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CourseViewMapper {

    private final ObjectMapper objectMapper;

    public CourseViewMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CourseResourceView toView(CourseInfoDO info, List<CourseResourceDO> resourceList) {
        CourseMetadata metadata = parseMetadata(info.getRemark());
        Map<String, String> resourceUrls = resourceList.stream()
                .filter(item -> item.getResourceType() != null && item.getResourceUrl() != null)
                .collect(Collectors.toMap(
                        CourseResourceDO::getResourceType,
                        CourseResourceDO::getResourceUrl,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        CourseResourceCategory category = resolveCategory(info, metadata);
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
        view.setPublishStatus(info.getPublishStatus());
        view.setTeacherId(info.getTeacherId());
        view.setResourceCount(resourceList.size());
        view.setCreatedAt(info.getCreateTime());
        view.setUpdatedAt(info.getUpdateTime());
        return view;
    }

    public CourseMetadata parseMetadata(String raw) {
        if (raw == null || raw.isBlank()) {
            return new CourseMetadata();
        }
        try {
            return objectMapper.readValue(raw, CourseMetadata.class);
        } catch (JsonProcessingException ex) {
            return new CourseMetadata();
        }
    }

    public CourseResourceCategory resolveCategory(CourseInfoDO info) {
        return resolveCategory(info, parseMetadata(info.getRemark()));
    }

    public String resolveSubjectCode(String moduleOrCode) {
        if (moduleOrCode == null || moduleOrCode.isBlank()) {
            return "geoscience";
        }
        String value = moduleOrCode.trim();
        return switch (value) {
            case "构造地质" -> "tectonics";
            case "岩石学" -> "petrology";
            case "地球化学" -> "geochemistry";
            case "古生物" -> "paleontology";
            case "地球物理" -> "geophysics";
            case "大气与海洋" -> "atmosphere-ocean";
            case "天文与空间" -> "astronomy-space";
            case "遥感" -> "remote-sensing";
            case "环境" -> "environment";
            case "语文" -> "chinese";
            case "数学" -> "math";
            case "英语" -> "english";
            default -> normalizeKnownSubjectCode(value);
        };
    }

    public String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private CourseResourceCategory resolveCategory(CourseInfoDO info, CourseMetadata metadata) {
        if (metadata.getCategory() != null && !metadata.getCategory().isBlank()) {
            return CourseResourceCategory.fromPath(metadata.getCategory());
        }
        String courseType = defaultValue(info.getCourseType(), "");
        return switch (courseType.toLowerCase(Locale.ROOT)) {
            case "teacher" -> CourseResourceCategory.TEACHER;
            case "interdisciplinary", "public" -> CourseResourceCategory.INTERDISCIPLINARY;
            default -> CourseResourceCategory.STUDENT;
        };
    }

    private String normalizeKnownSubjectCode(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "tectonics", "petrology", "geochemistry", "paleontology", "geophysics",
                 "atmosphere-ocean", "astronomy-space", "remote-sensing", "environment",
                 "chinese", "math", "english", "geoscience" -> normalized;
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

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
