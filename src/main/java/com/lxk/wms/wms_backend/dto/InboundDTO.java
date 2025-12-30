package com.lxk.wms.wms_backend.dto;

import com.lxk.wms.wms_backend.entity.InboundDetail;
import lombok.Data;
import java.util.List;

@Data
public class InboundDTO {
    // 接收前端表单的基础信息
    private Long id; // 如果是编辑需传ID
    private Long warehouseId;
    private String supplierName;
    private String remark;

    // 接收前端表格的产品列表
    private List<InboundDetail> products;
}