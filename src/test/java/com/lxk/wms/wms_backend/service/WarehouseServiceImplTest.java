package com.lxk.wms.wms_backend.service;

import com.lxk.wms.wms_backend.entity.Warehouse;
import com.lxk.wms.wms_backend.mapper.WarehouseMapper;
import com.lxk.wms.wms_backend.service.impl.WarehouseServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WarehouseServiceImplTest {

    @Mock private WarehouseMapper warehouseMapper;
    @InjectMocks private WarehouseServiceImpl warehouseService;

    @Test
    void saveWarehouse_unique_shouldCallSave() {
        ReflectionTestUtils.setField(warehouseService, "baseMapper", warehouseMapper);
        WarehouseServiceImpl spy = Mockito.spy(warehouseService);
        Warehouse w = new Warehouse();
        w.setWarehouseName("WH1");
        w.setWarehouseCode("W1");
        when(warehouseMapper.selectCount(any())).thenReturn(0L);
        doReturn(true).when(spy).saveOrUpdate(any(Warehouse.class));
        Assertions.assertTrue(spy.saveWarehouse(w));
    }

    @Test
    void saveWarehouse_duplicate_shouldThrow() {
        ReflectionTestUtils.setField(warehouseService, "baseMapper", warehouseMapper);
        Warehouse w = new Warehouse();
        w.setWarehouseName("WH1");
        w.setWarehouseCode("W1");
        when(warehouseMapper.selectCount(any())).thenReturn(1L);
        Assertions.assertThrows(RuntimeException.class, () -> warehouseService.saveWarehouse(w));
    }
}

