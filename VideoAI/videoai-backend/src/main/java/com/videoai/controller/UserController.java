package com.videoai.controller;

import com.videoai.common.api.BaseResponse;
import com.videoai.common.api.ResultUtils;
import com.videoai.model.annotation.LoginRequired;
import com.videoai.model.dto.user.UserLoginRequest;
import com.videoai.model.dto.user.UserRegisterRequest;
import com.videoai.model.vo.LoginVO;
import com.videoai.model.vo.UserVO;
import com.videoai.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public BaseResponse<LoginVO> register(@Valid @RequestBody UserRegisterRequest request) {
        return ResultUtils.success(userService.register(request));
    }

    @PostMapping("/login")
    public BaseResponse<LoginVO> login(@Valid @RequestBody UserLoginRequest request) {
        return ResultUtils.success(userService.login(request));
    }

    @GetMapping("/me")
    @LoginRequired
    public BaseResponse<UserVO> me() {
        return ResultUtils.success(userService.getCurrentUser());
    }

    @PostMapping("/logout")
    @LoginRequired
    public BaseResponse<Void> logout() {
        return ResultUtils.success();
    }
}
