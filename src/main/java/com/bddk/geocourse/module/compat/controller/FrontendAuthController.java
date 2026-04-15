package com.bddk.geocourse.module.compat.controller;

import com.bddk.geocourse.module.compat.model.FrontendAuthLoginRequest;
import com.bddk.geocourse.module.compat.model.FrontendAuthLoginResponse;
import com.bddk.geocourse.module.compat.model.FrontendAuthRegisterRequest;
import com.bddk.geocourse.module.compat.model.FrontendAuthUserView;
import com.bddk.geocourse.module.compat.service.FrontendAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
public class FrontendAuthController {

    private final FrontendAuthService frontendAuthService;

    public FrontendAuthController(FrontendAuthService frontendAuthService) {
        this.frontendAuthService = frontendAuthService;
    }

    @PostMapping("/login")
    public FrontendAuthLoginResponse login(@Valid @RequestBody FrontendAuthLoginRequest request) {
        return frontendAuthService.login(request);
    }

    @PostMapping("/register")
    public FrontendAuthLoginResponse register(@Valid @RequestBody FrontendAuthRegisterRequest request) {
        return frontendAuthService.register(request);
    }

    @GetMapping("/me")
    public FrontendAuthUserView me() {
        return frontendAuthService.currentUser();
    }

    @PutMapping("/teacher/profile")
    public FrontendAuthUserView updateTeacherProfile(@RequestParam String displayName,
                                                     @RequestParam(required = false) MultipartFile avatarFile,
                                                     @RequestParam(defaultValue = "false") boolean clearAvatar) {
        return frontendAuthService.updateTeacherProfile(displayName, avatarFile, clearAvatar);
    }
}
