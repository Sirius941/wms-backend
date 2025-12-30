package com.lxk.wms.wms_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction; // 注意导入这个
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxk.wms.wms_backend.entity.Warehouse;
import com.lxk.wms.wms_backend.mapper.WarehouseMapper;
import com.lxk.wms.wms_backend.service.WarehouseService;
import org.springframework.stereotype.Service;

@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {

    @Override
    public boolean saveWarehouse(Warehouse warehouse) {
        // 1. 校验名称唯一性 (编辑时会自动排除自己)
        checkUnique(warehouse.getId(), Warehouse::getWarehouseName, warehouse.getWarehouseName(), "仓库名称已存在");

        // 2. 校验编码唯一性
        checkUnique(warehouse.getId(), Warehouse::getWarehouseCode, warehouse.getWarehouseCode(), "仓库编码已存在");

        // 3. 核心：ID存在则更新，不存在则新增
        return this.saveOrUpdate(warehouse);
    }

    /**
     * 只有 Service 内部调用的私有方法，用于校验唯一性
     */
    private void checkUnique(Long id, SFunction<Warehouse, ?> column, Object value, String errorMsg) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(column, value);

        // 重点：如果是编辑模式（id不为空），排除当前这条数据
        if (id != null) {
            wrapper.ne(Warehouse::getId, id);
        }

        if (this.count(wrapper) > 0) {
            throw new RuntimeException(errorMsg);
        }
    }
}