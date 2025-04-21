package com.elwg.ai3dbackend.mapper;

import com.elwg.ai3dbackend.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 35245
* @description 针对表【user(用户表)】的数据库操作Mapper
* @createDate 2025-04-13 16:42:37
* @Entity com.elwg.ai3dbackend.model.entity.User
*/
public interface UserMapper extends BaseMapper<User> {

    /**
     * 物理删除用户记录，绕过MyBatis-Plus的逻辑删除
     *
     * @param id 用户ID
     * @return 影响行数
     */
    @Delete("DELETE FROM user WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);

    /**
     * 查询已被逻辑删除的用户，根据ID
     * 绕过MyBatis-Plus的逻辑删除条件
     *
     * @param id 用户ID
     * @return 用户实体，如果不存在则返回 null
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectByIdIgnoreLogicDelete(@Param("id") Long id);

    /**
     * 查询已被逻辑删除的用户，根据账号名称
     * 绕过MyBatis-Plus的逻辑删除条件
     *
     * @param userAccount 用户账号
     * @return 用户实体，如果不存在则返回 null
     */
    @Select("SELECT * FROM user WHERE userAccount = #{userAccount} AND isDelete = 1 LIMIT 1")
    User selectOneByAccountLogicDeleted(@Param("userAccount") String userAccount);

    /**
     * 查询所有已被逻辑删除的用户，根据账号名称
     * 绕过MyBatis-Plus的逻辑删除条件
     *
     * @param userAccount 用户账号
     * @return 用户实体列表
     */
    @Select("SELECT * FROM user WHERE userAccount = #{userAccount} AND isDelete = 1")
    List<User> selectListByAccountLogicDeleted(@Param("userAccount") String userAccount);

    /**
     * 查询所有用户数量，根据账号名称，包括逻辑删除的用户
     * 绕过MyBatis-Plus的逻辑删除条件
     *
     * @param userAccount 用户账号
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM user WHERE userAccount = #{userAccount}")
    long countByAccountIgnoreLogicDelete(@Param("userAccount") String userAccount);

    /**
     * 查询未被逻辑删除的用户数量，根据账号名称
     *
     * @param userAccount 用户账号
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM user WHERE userAccount = #{userAccount} AND isDelete = 0")
    long countByAccountActive(@Param("userAccount") String userAccount);
}




