package com.lxk.wms.wms_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wms_receive_task")
public class ReceiveTask {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long inboundId;
    private Long inboundDetailId;
    private Long productId;
    private Integer quantity;

    // 目标位置
    private Long warehouseId;
    private Long zoneId;
    private Long rackId;
    private Long binId;

    private Integer status; // 0-待执行 1-已完成 2-已取消
    private Long operatorId;
    private String remark;

    // --- 关联显示 (数据库不存在) ---
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String productSku;
    @TableField(exist = false)
    private String inboundNo; // 所属入库单号

    // 位置名称 (前端列表需要显示全路径: 仓库-库区-货架-库位)
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