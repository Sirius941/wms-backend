package com.lxk.wms.wms_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wms_zone")
public class Zone {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long warehouseId; // 关联的仓库ID

    // 数据库不存在该字段，用于前端展示仓库名称
    @TableField(exist = false)
    private String warehouseName;

    private String zoneName;
    private String zoneCode;
    private String zoneType;  // 例如：冷藏区、常温区
    private Integer status;   // 1-可用 0-不可用

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}