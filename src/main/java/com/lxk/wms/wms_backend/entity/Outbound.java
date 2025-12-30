package com.lxk.wms.wms_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("wms_outbound")
public class Outbound {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String outboundNo;    // 单号
    private Long warehouseId;     // 仓库
    private String customerName;  // 客户
    private Integer status;       // 0-待拣货 1-已发货 2-取消
    private String remark;

    // --- 关联显示 ---
    @TableField(exist = false)
    private String warehouseName;

    // --- 级联查询用：详情列表 ---
    @TableField(exist = false)
    private List<OutboundDetail> details;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}