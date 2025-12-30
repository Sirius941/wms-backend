package com.lxk.wms.wms_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wms_unit")
public class Unit {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String unitName; // 如：个、千克
    private String unitCode; // 如：PCS、KG
    private Integer status;  // 1-启用 0-禁用
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}