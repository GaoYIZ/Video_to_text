package com.videoai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("user")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userNo;

    private String username;

    private String password;

    private String nickname;

    private Integer status;

    /**
     * 总配额限制(已废弃,使用 dailyQuotaLimit 和 monthlyQuotaLimit)
     */
    @Deprecated
    private Integer quotaLimit;

    /**
     * 每日调用次数限制
     */
    private Integer dailyQuotaLimit;

    /**
     * 每月调用次数限制
     */
    private Integer monthlyQuotaLimit;
}
