package com.lxk.wms.wms_backend.dto;

import lombok.Data;

@Data
public class InventorySummaryDTO {
    private Long productId;
    private String productName;
    private String productSku;
    private String imgUrl;
    private String categoryName;
    private String unitName;

    private Long totalQuantity;  // 总库存
    private Long totalLocked;    // 总冻结
}