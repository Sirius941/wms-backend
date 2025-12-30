package com.lxk.wms.wms_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxk.wms.wms_backend.dto.InboundDTO;
import com.lxk.wms.wms_backend.entity.*;
import com.lxk.wms.wms_backend.mapper.*;
import com.lxk.wms.wms_backend.service.InboundService;
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
public class InboundServiceImpl extends ServiceImpl<InboundMapper, Inbound> implements InboundService {

    @Autowired private InboundDetailMapper inboundDetailMapper;
    @Autowired private WarehouseMapper warehouseMapper;
    @Autowired private ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务
    public void saveInbound(InboundDTO dto) {
        Inbound inbound = new Inbound();
        BeanUtils.copyProperties(dto, inbound);

        // 1. 处理主表
        if (inbound.getId() == null) {
            // 新增：生成单号 (IN + 时间 + 随机数)
            String no = "IN" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            inbound.setInboundNo(no);
            inbound.setStatus(0); // 默认 0-待收货
            this.save(inbound);
        } else {
            // 修改
            this.updateById(inbound);
            // 修改时，简单起见，先把旧的明细全删了，再插新的
            LambdaQueryWrapper<InboundDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(InboundDetail::getInboundId, inbound.getId());
            inboundDetailMapper.delete(wrapper);
        }

        // 2. 处理子表 (明细)
        List<InboundDetail> details = dto.getProducts();
        if (details != null && !details.isEmpty()) {
            for (InboundDetail item : details) {
                item.setInboundId(inbound.getId()); // 绑定主表ID
                item.setRealQuantity(0); // 新建时，实际收货为0
                inboundDetailMapper.insert(item);
            }
        }
    }

    @Override
    public IPage<Inbound> pageQuery(Integer pageNum, Integer pageSize, String inboundNo, Integer status) {
        LambdaQueryWrapper<Inbound> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(inboundNo)) wrapper.like(Inbound::getInboundNo, inboundNo);
        if (status != null) wrapper.eq(Inbound::getStatus, status);
        wrapper.orderByDesc(Inbound::getCreateTime);

        Page<Inbound> page = this.page(new Page<>(pageNum, pageSize), wrapper);

        // 填充仓库名称
        if (!page.getRecords().isEmpty()) {
            for (Inbound item : page.getRecords()) {
                Warehouse w = warehouseMapper.selectById(item.getWarehouseId());
                if (w != null) item.setWarehouseName(w.getWarehouseName());
            }
        }
        return page;
    }

    @Override
    public Inbound getDetailById(Long id) {
        Inbound inbound = this.getById(id);
        if (inbound != null) {
            // 查明细
            LambdaQueryWrapper<InboundDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(InboundDetail::getInboundId, id);
            List<InboundDetail> details = inboundDetailMapper.selectList(wrapper);

            // 填充产品信息 (前端要显示图片和SKU)
            if (!details.isEmpty()) {
                List<Long> pIds = details.stream().map(InboundDetail::getProductId).collect(Collectors.toList());
                Map<Long, Product> pMap = productMapper.selectBatchIds(pIds).stream()
                        .collect(Collectors.toMap(Product::getId, p -> p));

                for (InboundDetail d : details) {
                    Product p = pMap.get(d.getProductId());
                    if (p != null) {
                        d.setProductName(p.getProductName());
                        d.setProductSku(p.getSku());
                        d.setProductImg(p.getImgUrl());
                    }
                }
            }
            inbound.setDetails(details);
        }
        return inbound;
    }
}