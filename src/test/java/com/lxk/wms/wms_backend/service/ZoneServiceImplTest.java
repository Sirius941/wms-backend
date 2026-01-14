package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxk.wms.wms_backend.entity.Warehouse;
import com.lxk.wms.wms_backend.entity.Zone;
import com.lxk.wms.wms_backend.mapper.WarehouseMapper;
import com.lxk.wms.wms_backend.mapper.ZoneMapper;
import com.lxk.wms.wms_backend.service.impl.ZoneServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ZoneServiceImplTest {

    @Mock private ZoneMapper zoneMapper;
    @Mock private WarehouseMapper warehouseMapper;
    @InjectMocks private ZoneServiceImpl zoneService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(zoneService, "baseMapper", zoneMapper);
        lenient().when(zoneMapper.selectCount(any())).thenReturn(0L);
        lenient().when(zoneMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<Zone> req = invocation.getArgument(0);
            Page<Zone> resp = new Page<>(req.getCurrent(), req.getSize());
            Zone z1 = new Zone(); z1.setId(1L); z1.setZoneName("A"); z1.setWarehouseId(10L);
            Zone z2 = new Zone(); z2.setId(2L); z2.setZoneName("B"); z2.setWarehouseId(20L);
            resp.setRecords(Arrays.asList(z1, z2));
            resp.setTotal(2);
            return resp;
        });
        lenient().when(warehouseMapper.selectBatchIds(anyCollection())).thenAnswer(invocation -> {
            List<Long> ids = (List<Long>) invocation.getArgument(0);
            List<Warehouse> ws = Arrays.asList(
                    buildWarehouse(10L, "W10"), buildWarehouse(20L, "W20")
            );
            return ws;
        });
    }

    private Warehouse buildWarehouse(long id, String name) {
        Warehouse w = new Warehouse();
        w.setId(id); w.setWarehouseName(name);
        return w;
    }

    @Test
    void saveZone_unique_shouldSave() {
        ZoneServiceImpl spy = Mockito.spy(zoneService);
        Zone z = new Zone(); z.setZoneName("A"); z.setZoneCode("Z-A");
        doReturn(true).when(spy).saveOrUpdate(any(Zone.class));
        boolean ok = spy.saveZone(z);
        Assertions.assertTrue(ok);
    }

    @Test
    void saveZone_duplicate_shouldThrow() {
        Zone z = new Zone(); z.setZoneName("A"); z.setZoneCode("Z-A");
        Mockito.when(zoneMapper.selectCount(any())).thenReturn(1L);
        Assertions.assertThrows(RuntimeException.class, () -> zoneService.saveZone(z));
    }

    @Test
    void pageQuery_shouldFillWarehouseName() {
        IPage<Zone> page = zoneService.pageQuery(1, 10, null, null, null);
        Zone z = page.getRecords().get(0);
        Assertions.assertEquals("W10", z.getWarehouseName());
    }
}

