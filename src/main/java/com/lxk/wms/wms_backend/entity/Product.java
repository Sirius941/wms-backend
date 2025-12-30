package com.lxk.wms.wms_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wms_product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String sku;          // SKU (Stock Keeping Unit)
    private String productName;  // 产品名

    private Long categoryId;     // 分类ID
    private Long unitId;         // 单位ID
    private Long storageTypeId;  // 存放类型ID

    // --- 关联显示字段 (数据库不存在) ---
    @TableField(exist = false)
    private String categoryName;

    @TableField(exist = false)
    private String unitName;

    @TableField(exist = false)
    private String storageTypeName;
    // ----------------------------

    private String specs;    // 规格
    private String imgUrl;   // 图片地址
    private Integer status;  // 1-上架 0-下架
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}