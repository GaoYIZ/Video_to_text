package com.videoai.common.context;

import com.videoai.model.vo.LoginUserVO;

public final class LoginUserContext {

    private static final ThreadLocal<LoginUserVO> HOLDER = new ThreadLocal<>();

    private LoginUserContext() {
    }

    public static void set(LoginUserVO loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUserVO get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        return HOLDER.get() == null ? null : HOLDER.get().getUserId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
