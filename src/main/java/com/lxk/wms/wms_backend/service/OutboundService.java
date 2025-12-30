package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lxk.wms.wms_backend.dto.OutboundDTO;
import com.lxk.wms.wms_backend.entity.Outbound;

public interface OutboundService extends IService<Outbound> {
    void saveOutbound(OutboundDTO dto);
    IPage<Outbound> pageQuery(Integer pageNum, Integer pageSize, String outboundNo, Integer status);
    Outbound getDetailById(Long id);
}