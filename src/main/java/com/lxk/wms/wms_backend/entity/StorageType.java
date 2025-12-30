package com.lxk.wms.wms_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wms_storage_type")
public class StorageType {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String typeName; // 类型名称
    private String typeCode; // 类型编码
    private Integer status;  // 1-启用 0-禁用
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}