package com.lxk.wms.wms_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wms_product_tag")
public class ProductTag {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String tagName;
    private String tagCode;
    private String color;   // 颜色值，如 #FF0000
    private Integer status; // 1-启用 0-禁用
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}