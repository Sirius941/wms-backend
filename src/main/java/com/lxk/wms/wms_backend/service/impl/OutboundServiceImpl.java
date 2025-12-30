package com.lxk.wms.wms_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxk.wms.wms_backend.dto.OutboundDTO;
import com.lxk.wms.wms_backend.entity.*;
import com.lxk.wms.wms_backend.mapper.*;
import com.lxk.wms.wms_backend.service.OutboundService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OutboundServiceImpl extends ServiceImpl<OutboundMapper, Outbound> implements OutboundService {

    @Autowired private OutboundDetailMapper outboundDetailMapper;
    @Autowired private WarehouseMapper warehouseMapper;
    @Autowired private ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOutbound(OutboundDTO dto) {
        Outbound outbound = new Outbound();
        BeanUtils.copyProperties(dto, outbound);

        // 1. 处理主表
        if (outbound.getId() == null) {
            // 新增：OUT + 时间 + 随机
            String no = "OUT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            outbound.setOutboundNo(no);
            outbound.setStatus(0); // 0-待拣货
            this.save(outbound);
        } else {
            this.updateById(outbound);
            // 编辑模式：先删旧明细
            LambdaQueryWrapper<OutboundDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OutboundDetail::getOutboundId, outbound.getId());
            outboundDetailMapper.delete(wrapper);
        }

        // 2. 处理子表
        List<OutboundDetail> details = dto.getProducts();
        if (details != null && !details.isEmpty()) {
            for (OutboundDetail item : details) {
                item.setOutboundId(outbound.getId());
                item.setRealQuantity(0);
                outboundDetailMapper.insert(item);
            }
        }
    }

    @Override
    public IPage<Outbound> pageQuery(Integer pageNum, Integer pageSize, String outboundNo, Integer status) {
        LambdaQueryWrapper<Outbound> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(outboundNo)) wrapper.like(Outbound::getOutboundNo, outboundNo);
        if (status != null) wrapper.eq(Outbound::getStatus, status);
        wrapper.orderByDesc(Outbound::getCreateTime);

        Page<Outbound> page = this.page(new Page<>(pageNum, pageSize), wrapper);

        // 填充仓库名
        if (!page.getRecords().isEmpty()) {
            for (Outbound item : page.getRecords()) {
                Warehouse w = warehouseMapper.selectById(item.getWarehouseId());
                if (w != null) item.setWarehouseName(w.getWarehouseName());
            }
        }
        return page;
    }

    @Override
    public Outbound getDetailById(Long id) {
        Outbound outbound = this.getById(id);
        if (outbound != null) {
            LambdaQueryWrapper<OutboundDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OutboundDetail::getOutboundId, id);
            List<OutboundDetail> details = outboundDetailMapper.selectList(wrapper);

            // 填充产品信息
            if (!details.isEmpty()) {
                List<Long> pIds = details.stream().map(OutboundDetail::getProductId).collect(Collectors.toList());
                Map<Long, Product> pMap = productMapper.selectBatchIds(pIds).stream()
                        .collect(Collectors.toMap(Product::getId, p -> p));

                for (OutboundDetail d : details) {
                    Product p = pMap.get(d.getProductId());
                    if (p != null) {
                        d.setProductName(p.getProductName());
                        d.setProductSku(p.getSku());
                        d.setProductImg(p.getImgUrl());
                    }
                }
            }
            outbound.setDetails(details);
        }
        return outbound;
    }
}