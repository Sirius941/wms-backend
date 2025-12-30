package com.lxk.wms.wms_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxk.wms.wms_backend.entity.Unit;
import com.lxk.wms.wms_backend.mapper.UnitMapper;
import com.lxk.wms.wms_backend.service.UnitService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UnitServiceImpl extends ServiceImpl<UnitMapper, Unit> implements UnitService {

    @Override
    public boolean saveUnit(Unit unit) {
        // 1. 校验单位名称唯一性 (如不允许有两个"箱")
        checkUnique(unit.getId(), Unit::getUnitName, unit.getUnitName(), "单位名称已存在");

        // 2. 校验单位编码唯一性 (如不允许有两个"BOX")
        checkUnique(unit.getId(), Unit::getUnitCode, unit.getUnitCode(), "单位编码已存在");

        return this.saveOrUpdate(unit);
    }

    @Override
    public IPage<Unit> pageQuery(Integer pageNum, Integer pageSize, String name, Integer status) {
        LambdaQueryWrapper<Unit> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Unit::getUnitName, name);
        }
        if (status != null) {
            wrapper.eq(Unit::getStatus, status);
        }
        wrapper.orderByDesc(Unit::getCreateTime);

        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    private void checkUnique(Long id, SFunction<Unit, ?> column, Object value, String errorMsg) {
        LambdaQueryWrapper<Unit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(column, value);
        if (id != null) wrapper.ne(Unit::getId, id);
        if (this.count(wrapper) > 0) throw new RuntimeException(errorMsg);
    }
}