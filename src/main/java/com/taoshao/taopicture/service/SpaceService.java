package com.taoshao.taopicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.taoshao.taopicture.model.dto.space.SpaceAddRequest;
import com.taoshao.taopicture.model.dto.space.SpaceQueryRequest;
import com.taoshao.taopicture.model.entity.Space;
import com.taoshao.taopicture.model.entity.User;
import com.taoshao.taopicture.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author taoshao
 */
public interface SpaceService extends IService<Space> {

    /**
     * 创建空间
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 校验参数
     * @param space
     * @param add
     */
    void validSpace(Space space,boolean add);
    

    /**
     * 分页获取空间封装 （分页）
     * @param spacePage
     * @param request
     * @return
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 获取空间包装类（单条）
     * @param space
     * @param request
     * @return
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 查询条件
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);


    /**
     * 根据空间级别填充空间对象
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);
}
