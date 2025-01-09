package com.taoshao.taopicture.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 按照颜色搜索图片
 * @author taoshao
 */
@Data
public class SearchPictureByColorRequest implements Serializable {

    /**
     * 图片主色调
     */
    private String picColor;

    /**
     * 空间 id
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}
