package com.lxk.wms.wms_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wms_inventory")
public class Inventory {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;
    private Long warehouseId;
    private Long zoneId;
    private Long rackId;
    private Long binId;

    private String batchNo;       // ✅ 新增：批次号
    private Integer quantity;     // 现有库存 (可用 + 冻结)
    private Integer lockedQuantity; // ✅ 新增：冻结数量 (正在出库中的数量)

    // --- 关联显示字段 ---
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String productSku;
    @TableField(exist = false)
    private String productImg;
    @TableField(exist = false)
    private String unitName;
    @TableField(exist = false)
    private String warehouseName;
    @TableField(exist = false)
    private String zoneName;
    @TableField(exist = false)
    private String rackName;
    @TableField(exist = false)
    private String binCode;
    // -------------------

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}