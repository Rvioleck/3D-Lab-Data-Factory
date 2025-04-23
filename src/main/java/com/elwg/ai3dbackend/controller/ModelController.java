package com.elwg.ai3dbackend.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elwg.ai3dbackend.annotation.AuthCheck;
import com.elwg.ai3dbackend.common.BaseResponse;
import com.elwg.ai3dbackend.common.DeleteRequest;
import com.elwg.ai3dbackend.common.ResultUtils;
import com.elwg.ai3dbackend.constant.UserConstant;
import com.elwg.ai3dbackend.exception.BusinessException;
import com.elwg.ai3dbackend.exception.ErrorCode;
import com.elwg.ai3dbackend.exception.ThrowUtils;
import com.elwg.ai3dbackend.model.dto.model.ModelQueryRequest;
import com.elwg.ai3dbackend.model.dto.model.ModelUpdateRequest;
import com.elwg.ai3dbackend.model.entity.Model;
import com.elwg.ai3dbackend.model.entity.User;
import com.elwg.ai3dbackend.model.vo.ModelVO;
import com.elwg.ai3dbackend.service.ModelService;
import com.elwg.ai3dbackend.service.PictureService;
import com.elwg.ai3dbackend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 3D模型控制器
 * <p>
 * 提供3D模型管理相关的API接口
 * </p>
 */
@RestController
@RequestMapping("/model")
@Api(tags = "3D模型接口", description = "提供3D模型管理相关的功能")
@Slf4j
public class ModelController {

    @Resource
    private ModelService modelService;

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    /**
     * 获取模型详情
     * <p>
     * 该接口允许所有用户访问，用于获取单个模型的详细信息。
     * </p>
     *
     * @param id 模型ID
     * @return 模型详情
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "获取模型详情", notes = "根据模型ID获取模型详细信息")
    public BaseResponse<ModelVO> getModelById(@PathVariable Long id) {
        // 校验参数
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "模型ID不合法");

        // 查询模型
        Model model = modelService.getById(id);
        ThrowUtils.throwIf(model == null, ErrorCode.NOT_FOUND_ERROR, "模型不存在");

        // 转换为VO对象
        ModelVO modelVO = new ModelVO();
        BeanUtils.copyProperties(model, modelVO);

        return ResultUtils.success(modelVO);
    }

    /**
     * 分页获取模型列表
     * <p>
     * 该接口允许所有用户访问，用于分页获取模型列表。
     * 支持根据名称、分类、标签和用户ID进行过滤。
     * </p>
     *
     * @param modelQueryRequest 查询条件
     * @return 分页模型列表
     */
    @PostMapping("/list/page")
    @ApiOperation(value = "分页获取模型列表", notes = "根据条件分页获取模型列表")
    public BaseResponse<Page<ModelVO>> listModelByPage(@RequestBody ModelQueryRequest modelQueryRequest) {
        long current = modelQueryRequest.getCurrent();
        long size = modelQueryRequest.getPageSize();

        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "页面大小超过限制");

        // 构建查询条件
        LambdaQueryWrapper<Model> queryWrapper = new LambdaQueryWrapper<>();

        // 根据名称查询
        String name = modelQueryRequest.getName();
        if (StrUtil.isNotBlank(name)) {
            queryWrapper.like(Model::getName, name);
        }

        // 根据分类查询
        String category = modelQueryRequest.getCategory();
        if (StrUtil.isNotBlank(category)) {
            queryWrapper.eq(Model::getCategory, category);
        }

        // 根据标签查询
        String tags = modelQueryRequest.getTags();
        if (StrUtil.isNotBlank(tags)) {
            queryWrapper.like(Model::getTags, tags);
        }

        // 根据用户ID查询
        Long userId = modelQueryRequest.getUserId();
        if (userId != null && userId > 0) {
            queryWrapper.eq(Model::getUserId, userId);
        }

        // 根据状态查询
        String status = modelQueryRequest.getStatus();
        if (StrUtil.isNotBlank(status)) {
            queryWrapper.eq(Model::getStatus, status);
        }

        // 根据创建时间排序
        queryWrapper.orderByDesc(Model::getCreateTime);

        // 分页查询
        Page<Model> modelPage = modelService.page(new Page<>(current, size), queryWrapper);

        // 转换为VO对象
        Page<ModelVO> modelVOPage = new Page<>(modelPage.getCurrent(), modelPage.getSize(), modelPage.getTotal());
        List<ModelVO> modelVOList = modelPage.getRecords().stream().map(model -> {
            ModelVO modelVO = new ModelVO();
            BeanUtils.copyProperties(model, modelVO);
            return modelVO;
        }).collect(Collectors.toList());
        modelVOPage.setRecords(modelVOList);

        return ResultUtils.success(modelVOPage);
    }

    /**
     * 更新模型信息
     * <p>
     * 该接口允许模型创建者或管理员访问，用于更新模型信息。
     * </p>
     *
     * @param modelUpdateRequest 更新请求
     * @param request HTTP请求
     * @return 更新结果
     */
    @PostMapping("/update")
    @ApiOperation(value = "更新模型信息", notes = "更新模型的名称、简介、分类、标签等信息")
    @AuthCheck(mustRole = UserConstant.USER_ROLE)
    public BaseResponse<Boolean> updateModel(@RequestBody ModelUpdateRequest modelUpdateRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(modelUpdateRequest == null || modelUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 查询模型
        Long id = modelUpdateRequest.getId();
        Model model = modelService.getById(id);
        ThrowUtils.throwIf(model == null, ErrorCode.NOT_FOUND_ERROR, "模型不存在");

        // 仅本人或管理员可修改
        if (!model.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改");
        }

        // 更新模型信息
        Model modelUpdate = new Model();
        BeanUtils.copyProperties(modelUpdateRequest, modelUpdate);
        boolean result = modelService.updateById(modelUpdate);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新失败");

        return ResultUtils.success(true);
    }

    /**
     * 删除模型
     * <p>
     * 该接口允许模型创建者或管理员访问，用于删除模型。
     * </p>
     *
     * @param deleteRequest 删除请求
     * @param request HTTP请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除模型", notes = "删除指定ID的模型")
    @AuthCheck(mustRole = UserConstant.USER_ROLE)
    public BaseResponse<Boolean> deleteModel(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 查询模型
        Long id = deleteRequest.getId();
        Model model = modelService.getById(id);
        ThrowUtils.throwIf(model == null, ErrorCode.NOT_FOUND_ERROR, "模型不存在");

        // 仅本人或管理员可删除
        if (!model.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除");
        }

        // 删除模型记录
        boolean result = modelService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除失败");

        return ResultUtils.success(true);
    }

    /**
     * 获取模型分类列表
     * <p>
     * 该接口允许所有用户访问，用于获取系统中所有的模型分类。
     * </p>
     *
     * @return 分类列表
     */
    @GetMapping("/categories")
    @ApiOperation(value = "获取模型分类列表", notes = "获取系统中所有的模型分类")
    public BaseResponse<List<String>> getModelCategories() {
        // 查询所有不为空的分类
        LambdaQueryWrapper<Model> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Model::getCategory)
                .isNotNull(Model::getCategory)
                .ne(Model::getCategory, "");

        // 获取分类列表
        List<String> categories = modelService.list(queryWrapper).stream()
                .map(Model::getCategory)
                .distinct()
                .collect(Collectors.toList());

        return ResultUtils.success(categories);
    }

    /**
     * 批量获取模型信息
     * <p>
     * 该接口允许所有用户访问，用于批量获取模型信息。
     * </p>
     *
     * @param ids 模型ID列表
     * @return 模型信息列表
     */
    @PostMapping("/batch")
    @ApiOperation(value = "批量获取模型信息", notes = "根据ID列表批量获取模型信息")
    public BaseResponse<List<ModelVO>> batchGetModelByIds(@RequestBody List<Long> ids) {
        // 校验参数
        ThrowUtils.throwIf(ids == null || ids.isEmpty(), ErrorCode.PARAMS_ERROR);

        // 查询模型
        List<Model> models = modelService.listByIds(ids);

        // 转换为VO对象
        List<ModelVO> modelVOList = models.stream().map(model -> {
            ModelVO modelVO = new ModelVO();
            BeanUtils.copyProperties(model, modelVO);
            return modelVO;
        }).collect(Collectors.toList());

        return ResultUtils.success(modelVOList);
    }
}
