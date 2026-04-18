package com.videoai.security;

import com.videoai.common.context.LoginUserContext;
import com.videoai.common.enums.ErrorCode;
import com.videoai.common.exception.BusinessException;
import com.videoai.model.annotation.LoginRequired;
import com.videoai.model.vo.LoginUserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        boolean loginRequired = handlerMethod.getBeanType().isAnnotationPresent(LoginRequired.class)
                || handlerMethod.hasMethodAnnotation(LoginRequired.class);
        if (!loginRequired) {
            String token = jwtTokenProvider.resolveToken(request);
            if (token != null) {
                LoginUserContext.set(jwtTokenProvider.parseToken(token));
            }
            return true;
        }
        String token = jwtTokenProvider.resolveToken(request);
        if (token == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        LoginUserVO loginUser = jwtTokenProvider.parseToken(token);
        LoginUserContext.set(loginUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserContext.clear();
    }
}
