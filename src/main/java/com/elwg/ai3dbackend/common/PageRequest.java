package com.elwg.ai3dbackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求
 * 用于接收分页查询的参数
 */
@Data
public class PageRequest implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 当前页号
     */
    private long current = 1;

    /**
     * 页面大小
     */
    private long pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = "ascend";
}
