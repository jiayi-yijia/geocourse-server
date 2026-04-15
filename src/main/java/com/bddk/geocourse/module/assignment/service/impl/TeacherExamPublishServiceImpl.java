package com.bddk.geocourse.module.assignment.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.framework.common.error.ErrorCode;
import com.bddk.geocourse.framework.common.error.ServiceException;
import com.bddk.geocourse.module.assignment.entity.TeacherExamPublish;
import com.bddk.geocourse.module.assignment.entity.TeacherPaper;
import com.bddk.geocourse.module.assignment.mapper.TeacherExamPublishMapper;
import com.bddk.geocourse.module.assignment.mapper.TeacherPaperMapper;
import com.bddk.geocourse.module.assignment.model.TeacherExamPublishQuery;
import com.bddk.geocourse.module.assignment.model.TeacherExamPublishSaveRequest;
import com.bddk.geocourse.module.assignment.model.TeacherExamPublishView;
import com.bddk.geocourse.module.assignment.service.TeacherExamPublishService;
import com.bddk.geocourse.module.identity.service.TeacherPortalContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeacherExamPublishServiceImpl implements TeacherExamPublishService {

    private static final Set<String> PUBLISH_STATUSES = Set.of("DRAFT", "PUBLISHED", "CLOSED");

    private final TeacherPortalContextService teacherPortalContextService;
    private final TeacherExamPublishMapper teacherExamPublishMapper;
    private final TeacherPaperMapper teacherPaperMapper;

    public TeacherExamPublishServiceImpl(TeacherPortalContextService teacherPortalContextService,
                                         TeacherExamPublishMapper teacherExamPublishMapper,
                                         TeacherPaperMapper teacherPaperMapper) {
        this.teacherPortalContextService = teacherPortalContextService;
        this.teacherExamPublishMapper = teacherExamPublishMapper;
        this.teacherPaperMapper = teacherPaperMapper;
    }

    @Override
    public PageResult<TeacherExamPublishView> pagePublishes(TeacherExamPublishQuery query) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        long pageNo = query.getPageNo() <= 0 ? 1 : query.getPageNo();
        long pageSize = query.getPageSize() <= 0 ? 10 : Math.min(query.getPageSize(), 100);
        Page<TeacherExamPublish> page = teacherExamPublishMapper.selectPage(new Page<>(pageNo, pageSize),
                Wrappers.<TeacherExamPublish>lambdaQuery()
                        .eq(TeacherExamPublish::getTenantId, tenantId)
                        .like(StringUtils.hasText(query.getTitle()), TeacherExamPublish::getTitle, query.getTitle().trim())
                        .eq(query.getPaperId() != null, TeacherExamPublish::getPaperId, query.getPaperId())
                        .eq(StringUtils.hasText(query.getStatus()), TeacherExamPublish::getStatus, normalizeStatusAllowBlank(query.getStatus()))
                        .orderByDesc(TeacherExamPublish::getUpdateTime)
                        .orderByDesc(TeacherExamPublish::getId));
        Map<Long, String> paperNames = teacherPaperMapper.selectList(Wrappers.<TeacherPaper>lambdaQuery()
                        .eq(TeacherPaper::getTenantId, tenantId))
                .stream()
                .collect(Collectors.toMap(TeacherPaper::getId, TeacherPaper::getName, (left, right) -> left));
        return PageResult.of(page.getRecords().stream().map(item -> toView(item, paperNames.get(item.getPaperId()))).toList(),
                page.getTotal(), pageNo, pageSize);
    }

    @Override
    public TeacherExamPublishView getPublish(Long publishId) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        TeacherExamPublish publish = getPublishOrThrow(tenantId, publishId);
        TeacherPaper paper = getPaperOrThrow(tenantId, publish.getPaperId());
        return toView(publish, paper.getName());
    }

    @Override
    @Transactional
    public TeacherExamPublishView createPublish(TeacherExamPublishSaveRequest request) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = teacherPortalContextService.currentTeacherId();
        TeacherPaper paper = getPaperOrThrow(tenantId, request.getPaperId());
        validatePublishRequest(tenantId, request, null, paper);
        LocalDateTime now = LocalDateTime.now();

        TeacherExamPublish publish = new TeacherExamPublish();
        publish.setTenantId(tenantId);
        publish.setPaperId(paper.getId());
        publish.setTitle(trimToNull(request.getTitle()));
        publish.setDescription(trimToNull(request.getDescription()));
        publish.setStatus("DRAFT");
        publish.setStartTime(request.getStartTime());
        publish.setEndTime(request.getEndTime());
        publish.setPassScore(request.getPassScore());
        publish.setTotalScore(paper.getTotalScore());
        publish.setQuestionCount(paper.getQuestionCount());
        publish.setCreateBy(teacherId);
        publish.setUpdateBy(teacherId);
        publish.setCreateTime(now);
        publish.setUpdateTime(now);
        teacherExamPublishMapper.insert(publish);
        return toView(publish, paper.getName());
    }

    @Override
    @Transactional
    public TeacherExamPublishView updatePublish(Long publishId, TeacherExamPublishSaveRequest request) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        Long teacherId = teacherPortalContextService.currentTeacherId();
        TeacherExamPublish publish = getPublishOrThrow(tenantId, publishId);
        TeacherPaper paper = getPaperOrThrow(tenantId, request.getPaperId());
        validatePublishRequest(tenantId, request, publishId, paper);

        publish.setPaperId(paper.getId());
        publish.setTitle(trimToNull(request.getTitle()));
        publish.setDescription(trimToNull(request.getDescription()));
        publish.setStartTime(request.getStartTime());
        publish.setEndTime(request.getEndTime());
        publish.setPassScore(request.getPassScore());
        publish.setTotalScore(paper.getTotalScore());
        publish.setQuestionCount(paper.getQuestionCount());
        publish.setUpdateBy(teacherId);
        publish.setUpdateTime(LocalDateTime.now());
        teacherExamPublishMapper.updateById(publish);
        return toView(publish, paper.getName());
    }

    @Override
    @Transactional
    public void updatePublishStatus(Long publishId, String status) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        TeacherExamPublish publish = getPublishOrThrow(tenantId, publishId);
        publish.setStatus(normalizeStatus(status));
        publish.setUpdateBy(teacherPortalContextService.currentTeacherId());
        publish.setUpdateTime(LocalDateTime.now());
        teacherExamPublishMapper.updateById(publish);
    }

    @Override
    @Transactional
    public void deletePublish(Long publishId) {
        Long tenantId = teacherPortalContextService.currentTenantId();
        getPublishOrThrow(tenantId, publishId);
        teacherExamPublishMapper.deleteById(publishId);
    }

    private TeacherExamPublish getPublishOrThrow(Long tenantId, Long publishId) {
        TeacherExamPublish publish = teacherExamPublishMapper.selectOne(Wrappers.<TeacherExamPublish>lambdaQuery()
                .eq(TeacherExamPublish::getTenantId, tenantId)
                .eq(TeacherExamPublish::getId, publishId)
                .last("limit 1"));
        if (publish == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "考试发布不存在");
        }
        return publish;
    }

    private TeacherPaper getPaperOrThrow(Long tenantId, Long paperId) {
        TeacherPaper paper = teacherPaperMapper.selectOne(Wrappers.<TeacherPaper>lambdaQuery()
                .eq(TeacherPaper::getTenantId, tenantId)
                .eq(TeacherPaper::getId, paperId)
                .last("limit 1"));
        if (paper == null) {
            throw new ServiceException(ErrorCode.NOT_FOUND, "试卷不存在");
        }
        return paper;
    }

    private void validatePublishRequest(Long tenantId,
                                        TeacherExamPublishSaveRequest request,
                                        Long excludeId,
                                        TeacherPaper paper) {
        if (request.getEndTime().isBefore(request.getStartTime()) || request.getEndTime().isEqual(request.getStartTime())) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "截止时间必须晚于开考时间");
        }
        if (request.getPassScore().compareTo(paper.getTotalScore()) > 0) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "及格分不能大于试卷总分");
        }
        var query = Wrappers.<TeacherExamPublish>lambdaQuery()
                .eq(TeacherExamPublish::getTenantId, tenantId)
                .eq(TeacherExamPublish::getTitle, trimToNull(request.getTitle()));
        if (excludeId != null) {
            query.ne(TeacherExamPublish::getId, excludeId);
        }
        if (teacherExamPublishMapper.selectCount(query) > 0) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "已存在同名考试发布");
        }
    }

    private TeacherExamPublishView toView(TeacherExamPublish publish, String paperName) {
        TeacherExamPublishView view = new TeacherExamPublishView();
        view.setId(publish.getId());
        view.setPaperId(publish.getPaperId());
        view.setPaperName(paperName);
        view.setTitle(publish.getTitle());
        view.setDescription(publish.getDescription());
        view.setStatus(publish.getStatus());
        view.setStartTime(publish.getStartTime());
        view.setEndTime(publish.getEndTime());
        view.setPassScore(publish.getPassScore());
        view.setTotalScore(publish.getTotalScore());
        view.setQuestionCount(publish.getQuestionCount());
        view.setCreatedAt(publish.getCreateTime());
        view.setUpdatedAt(publish.getUpdateTime());
        return view;
    }

    private String normalizeStatus(String status) {
        String normalized = normalizeStatusAllowBlank(status);
        if (!StringUtils.hasText(normalized) || !PUBLISH_STATUSES.contains(normalized)) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "不支持的考试发布状态");
        }
        return normalized;
    }

    private String normalizeStatusAllowBlank(String status) {
        return StringUtils.hasText(status) ? status.trim().toUpperCase() : null;
    }

    private String trimToNull(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
