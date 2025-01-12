package com.taoshao.taopicture.constant;

/**
 * 用户常量
 *
 * @author taoshao
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    // endregion

    /**
     * 账号校验 数字和字母 ^[A-Za-z0-9]+$
     */
    String ACCOUNT_REGEX = "^[A-Za-z0-9]+$";

}
