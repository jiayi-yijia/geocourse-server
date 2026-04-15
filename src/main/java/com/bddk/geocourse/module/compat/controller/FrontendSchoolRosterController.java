package com.bddk.geocourse.module.compat.controller;

import com.bddk.geocourse.module.compat.model.FrontendAuthUserView;
import com.bddk.geocourse.module.compat.model.RosterStatusUpdateRequest;
import com.bddk.geocourse.module.compat.model.RosterStudentCreateRequest;
import com.bddk.geocourse.module.compat.model.RosterStudentView;
import com.bddk.geocourse.module.compat.model.RosterTeacherCreateRequest;
import com.bddk.geocourse.module.compat.model.RosterTeacherView;
import com.bddk.geocourse.module.compat.service.FrontendAuthService;
import com.bddk.geocourse.module.compat.service.FrontendRosterService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/school/roster")
public class FrontendSchoolRosterController {

    private final FrontendAuthService frontendAuthService;
    private final FrontendRosterService frontendRosterService;

    public FrontendSchoolRosterController(FrontendAuthService frontendAuthService,
                                          FrontendRosterService frontendRosterService) {
        this.frontendAuthService = frontendAuthService;
        this.frontendRosterService = frontendRosterService;
    }

    @GetMapping("/teachers")
    public List<RosterTeacherView> teachers() {
        frontendAuthService.requireCurrentSchoolAdmin();
        return frontendRosterService.listTeachers();
    }

    @GetMapping("/students")
    public List<RosterStudentView> students() {
        frontendAuthService.requireCurrentSchoolAdmin();
        return frontendRosterService.listStudents();
    }

    @PostMapping("/teachers")
    public RosterTeacherView createTeacher(@Valid @RequestBody RosterTeacherCreateRequest request) {
        FrontendAuthUserView current = frontendAuthService.requireCurrentSchoolAdmin();
        return frontendRosterService.createTeacher(request, current.school());
    }

    @PostMapping("/students")
    public RosterStudentView createStudent(@Valid @RequestBody RosterStudentCreateRequest request) {
        FrontendAuthUserView current = frontendAuthService.requireCurrentSchoolAdmin();
        return frontendRosterService.createStudent(request, current.school());
    }

    @PatchMapping("/teachers/{userId}/status")
    public RosterTeacherView updateTeacherStatus(@PathVariable Long userId,
                                                 @Valid @RequestBody RosterStatusUpdateRequest request) {
        frontendAuthService.requireCurrentSchoolAdmin();
        return frontendRosterService.updateTeacherStatus(userId, request.status());
    }

    @PatchMapping("/students/{userId}/status")
    public RosterStudentView updateStudentStatus(@PathVariable Long userId,
                                                 @Valid @RequestBody RosterStatusUpdateRequest request) {
        frontendAuthService.requireCurrentSchoolAdmin();
        return frontendRosterService.updateStudentStatus(userId, request.status());
    }
}
