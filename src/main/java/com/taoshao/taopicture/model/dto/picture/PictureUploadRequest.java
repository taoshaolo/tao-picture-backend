package com.taoshao.taopicture.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author taoshao
 * @Date 2025/1/5
 */
@Data
public class PictureUploadRequest implements Serializable {

    /**
     * 图片 id（用于修改）
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
