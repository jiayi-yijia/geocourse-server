package com.bddk.geocourse.module.compat.controller;

import com.bddk.geocourse.module.compat.model.FrontendAuthUserView;
import com.bddk.geocourse.module.compat.model.SchoolPortalAdminPayload;
import com.bddk.geocourse.module.compat.model.SchoolPortalContentOptionsResponse;
import com.bddk.geocourse.module.compat.model.SchoolPortalPublicResponse;
import com.bddk.geocourse.module.compat.service.FrontendAuthService;
import com.bddk.geocourse.module.compat.service.FrontendSchoolPortalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/school/portal")
public class FrontendSchoolPortalController {

    private final FrontendAuthService frontendAuthService;
    private final FrontendSchoolPortalService frontendSchoolPortalService;

    public FrontendSchoolPortalController(FrontendAuthService frontendAuthService,
                                          FrontendSchoolPortalService frontendSchoolPortalService) {
        this.frontendAuthService = frontendAuthService;
        this.frontendSchoolPortalService = frontendSchoolPortalService;
    }

    @GetMapping("/admin/config")
    public SchoolPortalAdminPayload adminConfig() {
        FrontendAuthUserView current = frontendAuthService.requireCurrentSchoolAdmin();
        return frontendSchoolPortalService.getAdminConfig(frontendAuthService.currentTenantId(), current.school());
    }

    @GetMapping("/admin/content-options")
    public SchoolPortalContentOptionsResponse contentOptions() {
        frontendAuthService.requireCurrentSchoolAdmin();
        return frontendSchoolPortalService.getContentOptions(frontendAuthService.currentTenantId());
    }

    @PutMapping("/admin/config")
    public SchoolPortalAdminPayload saveAdminConfig(@RequestBody SchoolPortalAdminPayload payload) {
        frontendAuthService.requireCurrentSchoolAdmin();
        return frontendSchoolPortalService.saveAdminConfig(frontendAuthService.currentTenantId(), payload);
    }

    @GetMapping("/public/{slug}")
    public SchoolPortalPublicResponse bySlug(@PathVariable String slug) {
        return frontendSchoolPortalService.findPublicBySlug(slug);
    }

    @GetMapping("/my")
    public SchoolPortalPublicResponse myPortal() {
        FrontendAuthUserView current = frontendAuthService.currentUser();
        return frontendSchoolPortalService.getMyPortal(frontendAuthService.currentTenantId(), current.school());
    }
}
