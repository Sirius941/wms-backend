package com.lxk.wms.wms_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wms_warehouse")
public class Warehouse {
    @TableId(type = IdType.AUTO) // 或者 IdType.ASSIGN_ID (雪花算法)
    private Long id;

    private String warehouseName;
    private String warehouseCode;
    private String address;
    private String principal;
    private String contactPhone;
    private Integer status; // 1-可用 0-不可用
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic // 逻辑删除注解
    private Integer isDeleted;
}