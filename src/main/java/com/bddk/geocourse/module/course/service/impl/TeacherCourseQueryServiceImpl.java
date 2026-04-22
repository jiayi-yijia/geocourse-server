package com.bddk.geocourse.module.course.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.module.course.dal.dataobject.CourseInfoDO;
import com.bddk.geocourse.module.course.dal.dataobject.CourseResourceDO;
import com.bddk.geocourse.module.course.dal.mapper.CourseInfoMapper;
import com.bddk.geocourse.module.course.dal.mapper.CourseResourceMapper;
import com.bddk.geocourse.module.course.model.CourseResourceCategory;
import com.bddk.geocourse.module.course.model.CourseResourceView;
import com.bddk.geocourse.module.course.model.TeacherCourseQuery;
import com.bddk.geocourse.module.course.service.TeacherCourseQueryService;
import com.bddk.geocourse.module.course.service.support.CourseViewMapper;
import com.bddk.geocourse.module.identity.service.TeacherPortalContextService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TeacherCourseQueryServiceImpl implements TeacherCourseQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final TeacherPortalContextService teacherPortalContextService;
    private final CourseInfoMapper courseInfoMapper;
    private final CourseResourceMapper courseResourceMapper;
    private final CourseViewMapper courseViewMapper;

    public TeacherCourseQueryServiceImpl(TeacherPortalContextService teacherPortalContextService,
                                         CourseInfoMapper courseInfoMapper,
                                         CourseResourceMapper courseResourceMapper,
                                         CourseViewMapper courseViewMapper) {
        this.teacherPortalContextService = teacherPortalContextService;
        this.courseInfoMapper = courseInfoMapper;
        this.courseResourceMapper = courseResourceMapper;
        this.courseViewMapper = courseViewMapper;
    }

    @Override
    public PageResult<CourseResourceView> pageCourses(TeacherCourseQuery query) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = Boolean.TRUE.equals(query.getMineOnly()) ? teacherPortalContextService.currentTeacherId() : null;
        long pageNo = normalizePageNo(query.getPageNo());
        long pageSize = normalizePageSize(query.getPageSize());

        String keyword = courseViewMapper.normalizeText(query.getKeyword());
        String publishStatus = courseViewMapper.normalizeText(query.getPublishStatus());
        String module = courseViewMapper.normalizeText(query.getModule());
        String stage = courseViewMapper.normalizeText(query.getStage());
        String difficulty = courseViewMapper.normalizeText(query.getDifficulty());
        CourseResourceCategory category = StringUtils.hasText(query.getCategory())
                ? CourseResourceCategory.fromPath(query.getCategory().trim())
                : null;

        List<CourseInfoDO> courseInfos = courseInfoMapper.selectList(Wrappers.<CourseInfoDO>lambdaQuery()
                .eq(CourseInfoDO::getTenantId, tenantId)
                .eq(CourseInfoDO::getStatus, 1)
                .eq(teacherId != null, CourseInfoDO::getTeacherId, teacherId)
                .eq(category != null, CourseInfoDO::getCourseType, category == null ? null : category.courseTypeValue())
                .eq(StringUtils.hasText(publishStatus), CourseInfoDO::getPublishStatus, publishStatus)
                .eq(StringUtils.hasText(module), CourseInfoDO::getSubjectCode,
                        module == null ? null : courseViewMapper.resolveSubjectCode(module))
                .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(CourseInfoDO::getCourseName, keyword)
                        .or()
                        .like(CourseInfoDO::getIntroText, keyword))
                .orderByDesc(CourseInfoDO::getUpdateTime)
                .orderByDesc(CourseInfoDO::getId));
        if (CollectionUtils.isEmpty(courseInfos)) {
            return PageResult.of(List.of(), 0, pageNo, pageSize);
        }

        Map<Long, List<CourseResourceDO>> resourceMap = loadResourceMap(tenantId,
                courseInfos.stream().map(CourseInfoDO::getId).toList());
        List<CourseResourceView> filteredViews = courseInfos.stream()
                .map(info -> courseViewMapper.toView(info, resourceMap.getOrDefault(info.getId(), List.of())))
                .filter(view -> matchesText(view.getStage(), stage))
                .filter(view -> matchesText(view.getDifficulty(), difficulty))
                .toList();

        long total = filteredViews.size();
        int fromIndex = (int) Math.min((pageNo - 1) * pageSize, total);
        int toIndex = (int) Math.min(fromIndex + pageSize, total);
        return PageResult.of(filteredViews.subList(fromIndex, toIndex), total, pageNo, pageSize);
    }

    @Override
    public CourseResourceView getCourse(Long courseId) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        CourseInfoDO courseInfo = courseInfoMapper.selectOne(Wrappers.<CourseInfoDO>lambdaQuery()
                .eq(CourseInfoDO::getTenantId, tenantId)
                .eq(CourseInfoDO::getId, courseId)
                .eq(CourseInfoDO::getStatus, 1)
                .last("limit 1"));
        if (courseInfo == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "Course not found");
        }
        Map<Long, List<CourseResourceDO>> resourceMap = loadResourceMap(tenantId, List.of(courseId));
        return courseViewMapper.toView(courseInfo, resourceMap.getOrDefault(courseId, List.of()));
    }

    private Map<Long, List<CourseResourceDO>> loadResourceMap(Long tenantId, List<Long> courseIds) {
        if (CollectionUtils.isEmpty(courseIds)) {
            return Map.of();
        }
        return courseResourceMapper.selectList(Wrappers.<CourseResourceDO>lambdaQuery()
                        .eq(CourseResourceDO::getTenantId, tenantId)
                        .in(CourseResourceDO::getCourseId, courseIds)
                        .eq(CourseResourceDO::getStatus, 1)
                        .orderByAsc(CourseResourceDO::getSortNo)
                        .orderByAsc(CourseResourceDO::getId))
                .stream()
                .collect(Collectors.groupingBy(CourseResourceDO::getCourseId, LinkedHashMap::new, Collectors.toList()));
    }

    private boolean matchesText(String actual, String expected) {
        if (!StringUtils.hasText(expected)) {
            return true;
        }
        return Objects.equals(courseViewMapper.normalizeText(actual), expected);
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
}
