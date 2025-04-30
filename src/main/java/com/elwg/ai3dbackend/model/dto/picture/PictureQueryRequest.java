package com.elwg.ai3dbackend.model.dto.picture;

import com.elwg.ai3dbackend.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 图片查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "图片查询请求")
public class PictureQueryRequest extends PageRequest implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 图片名称
     */
    @Schema(description = "图片名称", example = "示例图片")
    private String name;

    /**
     * 分类
     */
    @Schema(description = "分类", example = "容器")
    private String category;

    /**
     * 标签
     */
    @Schema(description = "标签", example = "锥形瓶,100ml")
    private String tags;

    /**
     * 创建用户 id
     */
    @Schema(description = "创建用户ID", example = "1")
    private Long userId;
}



