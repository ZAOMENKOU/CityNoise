package com.example.city_noise_system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

/**
 * 用户实体类
 */
@Data
@TableName("users")
@EqualsAndHashCode(callSuper = false)
public class User {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("real_name")
    private String realName;

    @TableField("role")
    private String role; // RESIDENT, WORKER, ADMIN

    @TableField("phone")
    private String phone;

    @TableField("avatar")
    private String avatar;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    // 逻辑删除字段
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}