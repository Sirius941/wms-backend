package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lxk.wms.wms_backend.entity.Warehouse;

public interface WarehouseService extends IService<Warehouse> {
    // 这里可以定义复杂的业务接口，比如“检查仓库是否还有库存”
    boolean saveWarehouse(Warehouse warehouse);
}
