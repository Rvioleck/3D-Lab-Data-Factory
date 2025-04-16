package com.elwg.ai3dbackend.controller;

import com.elwg.ai3dbackend.common.BaseResponse;
import com.elwg.ai3dbackend.common.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 * <p>
 * 用于测试各种功能和特性，包括序列化、反序列化、数据类型处理等。
 * 这些接口主要用于开发和测试环境，不应在生产环境中使用。
 * </p>
 */
@RestController
@RequestMapping("/test")
@Api(tags = "测试接口", description = "提供各种测试功能，仅用于开发和测试环境")
public class TestController {

    /**
     * 测试Long类型序列化
     * <p>
     * 返回包含各种类型ID的对象，用于测试前端如何处理不同类型的ID值，特别是Long类型的大数值。
     * 返回的数据包括：
     * 1. Long对象类型ID（longObjectId）
     * 2. long原始类型ID（longPrimitiveId）
     * 3. int类型ID（intId）
     * 4. 字符串类型ID（stringId）
     * 5. 包含Long类型ID的对象（object）
     * </p>
     * <p>
     * 该接口主要用于测试JSON序列化时对大数值的处理，确保前端能正确处理JavaScript数字精度限制问题。
     * </p>
     *
     * @return 包含各种类型ID的测试响应
     */
    @GetMapping("/long-serialization")
    @ApiOperation(value = "测试Long类型序列化", notes = "返回包含各种类型ID的对象，用于测试前端处理大数值")
    public BaseResponse<Map<String, Object>> testLongSerialization() {
        Map<String, Object> result = new HashMap<>();

        // 添加不同类型的ID
        result.put("longObjectId", Long.valueOf(123456789012345L));
        result.put("longPrimitiveId", 987654321098765L);
        result.put("intId", 100);
        result.put("stringId", "string-id-123");

        // 添加一个包含Long ID的对象
        TestObject testObject = new TestObject();
        testObject.setId(567890123456789L);
        testObject.setName("测试对象");
        result.put("object", testObject);

        return ResultUtils.success(result);
    }

    /**
     * 测试对象
     * <p>
     * 用于测试的简单对象类，包含Long类型的ID字段和字符串类型的name字段。
     * 主要用于测试对象序列化和反序列化。
     * </p>
     */
    @Data
    @ApiModel(value = "测试对象", description = "用于测试的简单对象，包含ID和名称")
    public static class TestObject {
        @ApiModelProperty(value = "ID", example = "567890123456789", notes = "测试用的Long类型ID，可能超过JavaScript数字精度限制")
        private Long id;

        @ApiModelProperty(value = "名称", example = "测试对象", notes = "对象的名称")
        private String name;
    }
}
