package com.bddk.geocourse.module.analytics.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bddk.geocourse.module.analytics.model.TeacherAnalyticsOverviewView;
import com.bddk.geocourse.module.analytics.service.TeacherAnalyticsService;
import com.bddk.geocourse.module.assignment.entity.TeacherExamRecord;
import com.bddk.geocourse.module.assignment.entity.TeacherPaper;
import com.bddk.geocourse.module.assignment.mapper.TeacherExamRecordMapper;
import com.bddk.geocourse.module.assignment.mapper.TeacherPaperMapper;
import com.bddk.geocourse.module.identity.dal.dataobject.SysRoleDO;
import com.bddk.geocourse.module.identity.dal.dataobject.SysUserRoleDO;
import com.bddk.geocourse.module.identity.dal.mapper.SysRoleMapper;
import com.bddk.geocourse.module.identity.dal.mapper.SysUserRoleMapper;
import com.bddk.geocourse.module.identity.service.TeacherPortalContextService;
import com.bddk.geocourse.module.questionbank.entity.Question;
import com.bddk.geocourse.module.questionbank.entity.QuestionCategory;
import com.bddk.geocourse.module.questionbank.mapper.QuestionCategoryMapper;
import com.bddk.geocourse.module.questionbank.mapper.QuestionMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeacherAnalyticsServiceImpl implements TeacherAnalyticsService {

    private final TeacherPortalContextService teacherPortalContextService;
    private final QuestionCategoryMapper questionCategoryMapper;
    private final QuestionMapper questionMapper;
    private final TeacherPaperMapper teacherPaperMapper;
    private final TeacherExamRecordMapper teacherExamRecordMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    public TeacherAnalyticsServiceImpl(TeacherPortalContextService teacherPortalContextService,
                                       QuestionCategoryMapper questionCategoryMapper,
                                       QuestionMapper questionMapper,
                                       TeacherPaperMapper teacherPaperMapper,
                                       TeacherExamRecordMapper teacherExamRecordMapper,
                                       SysRoleMapper sysRoleMapper,
                                       SysUserRoleMapper sysUserRoleMapper) {
        this.teacherPortalContextService = teacherPortalContextService;
        this.questionCategoryMapper = questionCategoryMapper;
        this.questionMapper = questionMapper;
        this.teacherPaperMapper = teacherPaperMapper;
        this.teacherExamRecordMapper = teacherExamRecordMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
    }

    @Override
    public TeacherAnalyticsOverviewView getOverview() {
        Long tenantId = teacherPortalContextService.currentTenantId();
        TeacherAnalyticsOverviewView view = new TeacherAnalyticsOverviewView();
        view.setCategoryCount(questionCategoryMapper.selectCount(Wrappers.<QuestionCategory>lambdaQuery()
                .eq(QuestionCategory::getTenantId, tenantId)));
        view.setQuestionCount(questionMapper.selectCount(Wrappers.<Question>lambdaQuery()
                .eq(Question::getTenantId, tenantId)));
        view.setPaperCount(teacherPaperMapper.selectCount(Wrappers.<TeacherPaper>lambdaQuery()
                .eq(TeacherPaper::getTenantId, tenantId)));
        view.setPublishedPaperCount(teacherPaperMapper.selectCount(Wrappers.<TeacherPaper>lambdaQuery()
                .eq(TeacherPaper::getTenantId, tenantId)
                .eq(TeacherPaper::getStatus, "PUBLISHED")));
        view.setExamCount(teacherExamRecordMapper.selectCount(Wrappers.<TeacherExamRecord>lambdaQuery()
                .eq(TeacherExamRecord::getTenantId, tenantId)));

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(23, 59, 59);
        view.setTodayExamCount(teacherExamRecordMapper.selectCount(Wrappers.<TeacherExamRecord>lambdaQuery()
                .eq(TeacherExamRecord::getTenantId, tenantId)
                .between(TeacherExamRecord::getCreateTime, start, end)));

        List<Long> teacherRoleIds = sysRoleMapper.selectList(Wrappers.<SysRoleDO>lambdaQuery()
                        .eq(SysRoleDO::getTenantId, tenantId)
                        .eq(SysRoleDO::getRoleCode, "teacher")
                        .eq(SysRoleDO::getStatus, 1))
                .stream()
                .map(SysRoleDO::getId)
                .toList();
        long teacherCount = teacherRoleIds.isEmpty() ? 0L : sysUserRoleMapper.selectList(Wrappers.<SysUserRoleDO>lambdaQuery()
                        .eq(SysUserRoleDO::getTenantId, tenantId)
                        .in(SysUserRoleDO::getRoleId, teacherRoleIds)
                        .eq(SysUserRoleDO::getStatus, 1))
                .stream()
                .map(SysUserRoleDO::getUserId)
                .distinct()
                .count();
        view.setTeacherCount(teacherCount);
        return view;
    }
}
