package com.elwg.ai3dbackend.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 通用分页响应
 * 用于封装分页查询的结果
 *
 * @param <T> 数据类型
 */
@Data
public class PageResponse<T> implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页数据列表
     */
    private List<T> records;

    /**
     * 当前页号
     */
    private long current;

    /**
     * 页面大小
     */
    private long pageSize;

    /**
     * 创建分页响应对象
     *
     * @param total    总记录数
     * @param records  当前页数据列表
     * @param current  当前页号
     * @param pageSize 页面大小
     * @param <T>      数据类型
     * @return 分页响应对象
     */
    public static <T> PageResponse<T> of(long total, List<T> records, long current, long pageSize) {
        PageResponse<T> pageResponse = new PageResponse<>();
        pageResponse.setTotal(total);
        pageResponse.setRecords(records);
        pageResponse.setCurrent(current);
        pageResponse.setPageSize(pageSize);
        return pageResponse;
    }
}
