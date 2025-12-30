package com.lxk.wms.wms_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wms_bin")
public class Bin {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 物理位置层级
    private Long rackId;      // 所属货架
    private Long zoneId;      // 所属库区 (冗余)
    private Long warehouseId; // 所属仓库 (冗余)

    // --- 前端展示用 (数据库不存在) ---
    @TableField(exist = false)
    private String rackName;
    @TableField(exist = false)
    private String zoneName;
    @TableField(exist = false)
    private String warehouseName;
    // ----------------------------

    private String binCode;   // 库位编码 (如 A-01-01)
    private Double capacity;  // 容量/承重

    // 0-空闲, 1-部分占用, 2-满载 (这是业务状态，由入库上架自动更新，人工一般不改)
    private Integer usageStatus;

    // 1-可用, 0-禁用 (这是物理状态，比如库位坏了)
    private Integer status;


    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}