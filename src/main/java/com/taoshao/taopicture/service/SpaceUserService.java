package com.taoshao.taopicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taoshao.taopicture.model.dto.spaceuser.SpaceUserAddRequest;
import com.taoshao.taopicture.model.dto.spaceuser.SpaceUserQueryRequest;
import com.taoshao.taopicture.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.taoshao.taopicture.model.vo.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *
 */
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 添加空间成员
     * @param spaceUserAddRequest
     * @return
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 校验空间成员
     * @param spaceUser
     * @param add
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 获取查询条件
     * @param spaceUserQueryRequest
     * @return
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 获取空间成员视图对象
     * @param spaceUser
     * @param request
     * @return
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 获取空间成员视图对象列表
     * @param spaceUserList
     * @return
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);
}
