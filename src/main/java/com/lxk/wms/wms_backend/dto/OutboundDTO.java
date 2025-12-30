package com.lxk.wms.wms_backend.dto;

import com.lxk.wms.wms_backend.entity.OutboundDetail;
import lombok.Data;
import java.util.List;

@Data
public class OutboundDTO {
    private Long id;
    private Long warehouseId;
    private String customerName;
    private String remark;

    // 前端传来的产品列表
    private List<OutboundDetail> products;
}