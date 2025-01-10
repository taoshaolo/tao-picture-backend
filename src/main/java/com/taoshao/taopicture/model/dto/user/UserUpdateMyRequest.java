package com.taoshao.taopicture.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户更新个人信息请求
 *
 * @author taoshao
 */
@Data
public class UserUpdateMyRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户性别 0-男 1-女
     */
    private Integer sex;

    /**
     * 简介
     */
    private String userProfile;

    private static final long serialVersionUID = 1L;
}