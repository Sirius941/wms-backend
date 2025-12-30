package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lxk.wms.wms_backend.dto.InboundDTO;
import com.lxk.wms.wms_backend.entity.Inbound;

public interface InboundService extends IService<Inbound> {
    // 创建/保存入库单 (使用DTO接收)
    void saveInbound(InboundDTO inboundDTO);

    // 分页查询
    IPage<Inbound> pageQuery(Integer pageNum, Integer pageSize, String inboundNo, Integer status);

    // 获取详情 (包含明细列表)
    Inbound getDetailById(Long id);
}