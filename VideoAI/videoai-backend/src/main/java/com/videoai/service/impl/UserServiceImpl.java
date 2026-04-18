package com.videoai.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.videoai.common.context.LoginUserContext;
import com.videoai.common.enums.ErrorCode;
import com.videoai.common.exception.BusinessException;
import com.videoai.common.util.IdUtils;
import com.videoai.mapper.UserMapper;
import com.videoai.model.dto.user.UserLoginRequest;
import com.videoai.model.dto.user.UserRegisterRequest;
import com.videoai.model.entity.User;
import com.videoai.model.vo.LoginUserVO;
import com.videoai.model.vo.LoginVO;
import com.videoai.model.vo.UserVO;
import com.videoai.security.JwtTokenProvider;
import com.videoai.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserMapper userMapper, JwtTokenProvider jwtTokenProvider) {
        this.userMapper = userMapper;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public LoginVO register(UserRegisterRequest request) {
        Long count = userMapper.selectCount(Wrappers.<User>lambdaQuery().eq(User::getUsername, request.getUsername()));
        if (count != null && count > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_REQUEST, "用户名已存在");
        }
        User user = new User();
        user.setUserNo(IdUtils.userNo());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setStatus(1);
        user.setQuotaLimit(10000);
        userMapper.insert(user);
        return buildLoginVO(user);
    }

    @Override
    public LoginVO login(UserLoginRequest request) {
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        return buildLoginVO(user);
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return UserVO.builder()
                .id(user.getId())
                .userNo(user.getUserNo())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .build();
    }

    private LoginVO buildLoginVO(User user) {
        String token = jwtTokenProvider.createToken(LoginUserVO.builder()
                .userId(user.getId())
                .userNo(user.getUserNo())
                .username(user.getUsername())
                .build());
        return LoginVO.builder()
                .token(token)
                .user(UserVO.builder()
                        .id(user.getId())
                        .userNo(user.getUserNo())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .build())
                .build();
    }
}
