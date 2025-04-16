package com.elwg.ai3dbackend.controller;

import com.elwg.ai3dbackend.common.BaseResponse;
import com.elwg.ai3dbackend.common.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 * 用于测试各种功能
 */
@RestController
@RequestMapping("/test")
@Api(tags = "测试接口")
public class TestController {

    /**
     * 测试Long类型序列化
     * 返回包含各种类型ID的对象
     *
     * @return 测试响应
     */
    @GetMapping("/long-serialization")
    @ApiOperation("测试Long类型序列化")
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
     * 包含Long类型的ID字段
     */
    @Data
    public static class TestObject {
        private Long id;
        private String name;
    }
}
