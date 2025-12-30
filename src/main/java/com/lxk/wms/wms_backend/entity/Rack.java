package com.lxk.wms.wms_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wms_rack")
public class Rack {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long zoneId;      // 所属库区
    private Long warehouseId; // 所属仓库 (冗余字段，方便快速查询)

    // --- 以下两个字段数据库没有，仅用于前端展示 ---
    @TableField(exist = false)
    private String zoneName;

    @TableField(exist = false)
    private String warehouseName;
    // ----------------------------------------

    private String rackName;  // 货架名称/编号
    private String rackCode;  // 货架编码 (唯一)
    private Integer status;   // 1-可用 0-不可用

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}