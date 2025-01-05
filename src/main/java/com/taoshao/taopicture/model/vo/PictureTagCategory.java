package com.taoshao.taopicture.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 标签分类列表视图
 *
 * @Author taoshao
 * @Date 2025/1/5
 */
@Data
public class PictureTagCategory implements Serializable {

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 分类列表
     */
    private List<String> categoryList;

}

