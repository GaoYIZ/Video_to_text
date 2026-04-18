package com.videoai.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;

public final class IdUtils {

    private IdUtils() {
    }

    public static String userNo() {
        return "U" + DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(4);
    }

    public static String fileNo() {
        return "F" + DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(4);
    }

    public static String uploadId() {
        return "UP" + DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(6);
    }

    public static String taskNo() {
        return "VT" + DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(6);
    }
}
