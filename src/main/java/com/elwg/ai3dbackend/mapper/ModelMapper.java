package com.elwg.ai3dbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elwg.ai3dbackend.model.entity.Model;
import org.apache.ibatis.annotations.Mapper;

/**
 * 3D模型Mapper接口
 */
@Mapper
public interface ModelMapper extends BaseMapper<Model> {
}
