package com.lxk.wms.wms_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wms_pick_task")
public class PickTask {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long outboundId;
    private Long outboundDetailId;
    private Long productId;
    private Integer quantity;

    // 源位置 (Source Location)
    private Long warehouseId;
    private Long zoneId;
    private Long rackId;
    private Long binId;

    private Integer status; // 0-待执行 1-已完成 2-已取消
    private Long operatorId;
    private String remark;

    // --- 关联显示 ---
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String productSku;
    @TableField(exist = false)
    private String outboundNo;

    // 位置名称
    @TableField(exist = false)
    private String warehouseName;
    @TableField(exist = false)
    private String binCode;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}