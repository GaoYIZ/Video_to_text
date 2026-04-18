package com.videoai.service;

import com.videoai.model.dto.user.UserLoginRequest;
import com.videoai.model.dto.user.UserRegisterRequest;
import com.videoai.model.vo.LoginVO;
import com.videoai.model.vo.UserVO;

public interface UserService {

    LoginVO register(UserRegisterRequest request);

    LoginVO login(UserLoginRequest request);

    UserVO getCurrentUser();
}
