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
import com.elwg.ai3dbackend.model.dto.picture.PictureQueryRequest;
import com.elwg.ai3dbackend.model.dto.picture.PictureUpdateRequest;
import com.elwg.ai3dbackend.model.entity.Picture;
import com.elwg.ai3dbackend.model.entity.User;
import com.elwg.ai3dbackend.model.vo.PictureVO;
import com.elwg.ai3dbackend.service.FileStorageService;
import com.elwg.ai3dbackend.service.ModelService;
import com.elwg.ai3dbackend.service.PictureService;
import com.elwg.ai3dbackend.service.UserService;
import com.elwg.ai3dbackend.utils.ImageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 图片资源控制器
 * <p>
 * 提供图片资源管理相关的API接口
 * </p>
 */
@RestController
@RequestMapping("/picture")
@Api(tags = "图片资源接口", description = "提供图片资源管理相关的功能")
@Slf4j
public class PictureController {

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @Resource
    private ModelService modelService;

    /**
     * 上传图片
     * <p>
     * 该接口允许所有已登录用户访问，用于上传图片文件并保存图片信息。
     * 支持设置图片名称、分类、标签和简介等信息。
     * </p>
     *
     * @param file         图片文件
     * @param name         图片名称（可选）
     * @param category     图片分类（可选）
     * @param tags         图片标签，逗号分隔（可选）
     * @param introduction 图片简介（可选）
     * @param request      HTTP请求
     * @return 包含上传结果的响应对象
     */
    @PostMapping("/upload")
    @ApiOperation(value = "上传图片", notes = "上传图片文件并保存图片信息")
    @AuthCheck(mustRole = UserConstant.USER_ROLE)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "introduction", required = false) String introduction,
            HttpServletRequest request) {

        // 获取当前登录用户 - 通过AuthCheck注解已经确保用户已登录
        User loginUser = userService.getLoginUser(request);

        // 校验文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件为空");
        }

        // 校验文件大小
        long fileSize = file.getSize();
        // 限制文件大小为10MB
        final long FILE_SIZE_LIMIT = 10 * 1024 * 1024L;
        if (fileSize > FILE_SIZE_LIMIT) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小超过限制");
        }

        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        log.info("上传图片: 文件名={}, 类型={}, 大小={}", originalFilename, contentType, fileSize);

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误，仅支持图片");
        }

        try {
            // 生成文件路径
            String uuid = UUID.randomUUID().toString();
            String fileExtension = getFileExtension(originalFilename);
            String filePath = String.format("images/%s%s", uuid, fileExtension);

            // 上传文件
            fileStorageService.saveFile(filePath, file.getInputStream());

            // 获取文件URL
            String fileUrl = fileStorageService.getFileUrl(filePath);

            // 获取图片信息
            Map<String, Object> imageInfo = ImageUtils.getImageInfo(file.getInputStream());

            // 创建图片记录
            Picture picture = new Picture();
            picture.setUrl(fileUrl);
            picture.setName(StrUtil.isNotBlank(name) ? name : originalFilename);
            picture.setCategory(category);
            picture.setTags(tags);
            picture.setIntroduction(introduction);
            picture.setPicSize(fileSize);
            picture.setPicFormat(fileExtension.substring(1)); // 去掉点号
            picture.setUserId(loginUser.getId());

            // 设置图片尺寸信息
            if (imageInfo != null) {
                Integer width = (Integer) imageInfo.get("width");
                Integer height = (Integer) imageInfo.get("height");
                picture.setPicWidth(width);
                picture.setPicHeight(height);
                if (width != null && height != null && height != 0) {
                    picture.setPicScale((double) width / height);
                }
            }

            // 保存图片记录
            boolean saveResult = pictureService.save(picture);
            ThrowUtils.throwIf(!saveResult, ErrorCode.OPERATION_ERROR, "图片信息保存失败");

            // 转换为VO对象
            PictureVO pictureVO = new PictureVO();
            BeanUtils.copyProperties(picture, pictureVO);

            // 设置是否有关联的3D模型
            pictureVO.setHasModel(modelService.getModelBySourceImageId(picture.getId()) != null);

            return ResultUtils.success(pictureVO);
        } catch (IOException e) {
            log.error("图片上传失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片上传失败");
        }
    }

    /**
     * 获取图片详情
     * <p>
     * 该接口允许所有用户访问，用于获取单个图片的详细信息。
     * </p>
     *
     * @param id 图片ID
     * @return 图片详情
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "获取图片详情", notes = "根据图片ID获取图片详细信息")
    public BaseResponse<PictureVO> getPictureById(@PathVariable Long id) {
        // 校验参数
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "图片ID不合法");

        // 查询图片
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

        // 转换为VO对象
        PictureVO pictureVO = new PictureVO();
        BeanUtils.copyProperties(picture, pictureVO);

        // 设置是否有关联的3D模型
        pictureVO.setHasModel(modelService.getModelBySourceImageId(picture.getId()) != null);

        return ResultUtils.success(pictureVO);
    }

    /**
     * 分页获取图片列表
     * <p>
     * 该接口允许所有用户访问，用于分页获取图片列表。
     * 支持根据名称、分类、标签和用户ID进行过滤。
     * </p>
     *
     * @param pictureQueryRequest 查询条件
     * @return 分页图片列表
     */
    @PostMapping("/list/page")
    @ApiOperation(value = "分页获取图片列表", notes = "根据条件分页获取图片列表")
    public BaseResponse<Page<PictureVO>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();

        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "页面大小超过限制");

        // 构建查询条件
        LambdaQueryWrapper<Picture> queryWrapper = new LambdaQueryWrapper<>();

        // 根据名称查询
        String name = pictureQueryRequest.getName();
        if (StrUtil.isNotBlank(name)) {
            queryWrapper.like(Picture::getName, name);
        }

        // 根据分类查询
        String category = pictureQueryRequest.getCategory();
        if (StrUtil.isNotBlank(category)) {
            queryWrapper.eq(Picture::getCategory, category);
        }

        // 根据标签查询
        String tags = pictureQueryRequest.getTags();
        if (StrUtil.isNotBlank(tags)) {
            queryWrapper.like(Picture::getTags, tags);
        }

        // 根据用户ID查询
        Long userId = pictureQueryRequest.getUserId();
        if (userId != null && userId > 0) {
            queryWrapper.eq(Picture::getUserId, userId);
        }

        // 根据创建时间排序
        queryWrapper.orderByDesc(Picture::getCreateTime);

        // 分页查询
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), queryWrapper);

        // 获取图片ID列表
        List<Long> pictureIds = picturePage.getRecords().stream()
                .map(Picture::getId)
                .collect(Collectors.toList());

        // 查询图片是否有关联的3D模型
        Map<Long, Boolean> pictureHasModelMap = new HashMap<>();
        if (!pictureIds.isEmpty()) {
            for (Long pictureId : pictureIds) {
                pictureHasModelMap.put(pictureId, modelService.getModelBySourceImageId(pictureId) != null);
            }
        }

        // 转换为VO对象
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        List<PictureVO> pictureVOList = picturePage.getRecords().stream().map(picture -> {
            PictureVO pictureVO = new PictureVO();
            BeanUtils.copyProperties(picture, pictureVO);
            // 设置是否有关联的3D模型
            pictureVO.setHasModel(pictureHasModelMap.getOrDefault(picture.getId(), false));
            return pictureVO;
        }).collect(Collectors.toList());
        pictureVOPage.setRecords(pictureVOList);

        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 更新图片信息
     * <p>
     * 该接口允许已登录用户更新自己的图片信息，管理员可以更新任何图片。
     * 更新成功后返回 true。
     * </p>
     *
     * @param pictureUpdateRequest 更新请求
     * @param request              HTTP请求
     * @return 更新结果
     */
    @PostMapping("/update")
    @ApiOperation(value = "更新图片信息", notes = "更新图片的基本信息")
    @AuthCheck(mustRole = UserConstant.USER_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest,
                                               HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(pictureUpdateRequest == null, ErrorCode.PARAMS_ERROR);

        // 获取当前登录用户 - 通过AuthCheck注解已经确保用户已登录
        User loginUser = userService.getLoginUser(request);

        // 查询图片
        Long id = pictureUpdateRequest.getId();
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

        // 仅本人或管理员可修改
        if (!picture.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改");
        }

        // 更新图片信息
        Picture pictureUpdate = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, pictureUpdate);
        boolean result = pictureService.updateById(pictureUpdate);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新失败");

        return ResultUtils.success(true);
    }

    /**
     * 删除图片
     * <p>
     * 该接口允许已登录用户删除自己的图片，管理员可以删除任何图片。
     * 如果图片已关联3D模型，则无法删除。
     * 删除成功后返回 true。
     * </p>
     *
     * @param deleteRequest 删除请求
     * @param request       HTTP请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除图片", notes = "删除图片及其存储文件")
    @AuthCheck(mustRole = UserConstant.USER_ROLE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest,
                                               HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);

        // 获取当前登录用户 - 通过AuthCheck注解已经确保用户已登录
        User loginUser = userService.getLoginUser(request);

        // 查询图片
        Long id = deleteRequest.getId();
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

        // 仅本人或管理员可删除
        if (!picture.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除");
        }

        // 检查是否有关联的3D模型
        if (modelService.getModelBySourceImageId(id) != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该图片已关联3D模型，无法删除");
        }

        // 删除图片记录
        boolean result = pictureService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除失败");


        return ResultUtils.success(true);
    }

    /**
     * 获取图片分类列表
     * <p>
     * 该接口允许所有用户访问，用于获取系统中所有的图片分类。
     * </p>
     *
     * @return 分类列表
     */
    @GetMapping("/categories")
    @ApiOperation(value = "获取图片分类列表", notes = "获取系统中所有的图片分类")
    public BaseResponse<List<String>> getPictureCategories() {
        // 查询所有不重复的分类
        List<String> categories = pictureService.listCategories();
        return ResultUtils.success(categories);
    }

    /**
     * 获取图片标签列表
     * <p>
     * 该接口允许所有用户访问，用于获取系统中所有的图片标签。
     * </p>
     *
     * @return 标签列表
     */
    @GetMapping("/tags")
    @ApiOperation(value = "获取图片标签列表", notes = "获取系统中所有的图片标签")
    public BaseResponse<List<String>> getPictureTags() {
        // 查询所有不重复的标签
        List<String> tags = pictureService.listTags();
        return ResultUtils.success(tags);
    }

    /**
     * 批量获取图片信息
     * <p>
     * 该接口允许所有用户访问，用于批量获取图片信息。
     * </p>
     *
     * @param ids 图片ID列表
     * @return 图片信息列表
     */
    @PostMapping("/batch")
    @ApiOperation(value = "批量获取图片信息", notes = "根据ID列表批量获取图片信息")
    public BaseResponse<List<PictureVO>> batchGetPictureByIds(@RequestBody List<Long> ids) {
        // 校验参数
        ThrowUtils.throwIf(ids == null || ids.isEmpty(), ErrorCode.PARAMS_ERROR);

        // 查询图片
        List<Picture> pictures = pictureService.listByIds(ids);

        // 获取图片ID列表
        List<Long> pictureIds = pictures.stream()
                .map(Picture::getId)
                .collect(Collectors.toList());

        // 查询图片是否有关联的3D模型
        Map<Long, Boolean> pictureHasModelMap = new HashMap<>();
        if (!pictureIds.isEmpty()) {
            for (Long pictureId : pictureIds) {
                pictureHasModelMap.put(pictureId, modelService.getModelBySourceImageId(pictureId) != null);
            }
        }

        // 转换为VO对象
        List<PictureVO> pictureVOList = pictures.stream().map(picture -> {
            PictureVO pictureVO = new PictureVO();
            BeanUtils.copyProperties(picture, pictureVO);
            // 设置是否有关联的3D模型
            pictureVO.setHasModel(pictureHasModelMap.getOrDefault(picture.getId(), false));
            return pictureVO;
        }).collect(Collectors.toList());

        return ResultUtils.success(pictureVOList);
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 文件扩展名（包含点号）
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty() || !fileName.contains(".")) {
            return ".png"; // 默认扩展名
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
