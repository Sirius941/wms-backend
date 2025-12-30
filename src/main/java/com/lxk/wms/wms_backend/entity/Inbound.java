package com.lxk.wms.wms_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("wms_inbound")
public class Inbound {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String inboundNo;     // 单号
    private Long warehouseId;     // 仓库ID
    private String supplierName;  // 供应商
    private Integer status;       // 0-待收货 1-已收货 2-作废
    private String remark;

    // --- 关联显示 ---
    @TableField(exist = false)
    private String warehouseName;

    // --- 级联查询用：详情列表 ---
    @TableField(exist = false)
    private List<InboundDetail> details;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}