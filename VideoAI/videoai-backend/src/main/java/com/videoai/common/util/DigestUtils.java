package com.videoai.common.util;

import cn.hutool.crypto.digest.MD5;

public final class DigestUtils {

    private DigestUtils() {
    }

    public static String md5(String value) {
        return MD5.create().digestHex(value);
    }
}
