package com.lxk.wms.wms_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("wms_inbound_detail")
public class InboundDetail {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long inboundId;   // 父ID
    private Long productId;   // 产品ID
    private Integer planQuantity; // 计划数
    private Integer realQuantity; // 实际数
    private String remark;

    // --- 关联显示 ---
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String productSku;
    @TableField(exist = false)
    private String productImg;

    @TableLogic
    private Integer isDeleted;
}