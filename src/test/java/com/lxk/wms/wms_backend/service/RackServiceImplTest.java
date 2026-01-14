package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxk.wms.wms_backend.entity.Rack;
import com.lxk.wms.wms_backend.entity.Zone;
import com.lxk.wms.wms_backend.entity.Warehouse;
import com.lxk.wms.wms_backend.mapper.RackMapper;
import com.lxk.wms.wms_backend.mapper.ZoneMapper;
import com.lxk.wms.wms_backend.mapper.WarehouseMapper;
import com.lxk.wms.wms_backend.service.impl.RackServiceImpl;
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
public class RackServiceImplTest {

    @Mock private RackMapper rackMapper;
    @Mock private ZoneMapper zoneMapper;
    @Mock private WarehouseMapper warehouseMapper;
    @InjectMocks private RackServiceImpl rackService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(rackService, "baseMapper", rackMapper);
        lenient().when(rackMapper.selectCount(any())).thenReturn(0L);
        lenient().when(rackMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<Rack> req = invocation.getArgument(0);
            Page<Rack> resp = new Page<>(req.getCurrent(), req.getSize());
            Rack r1 = new Rack(); r1.setId(1L); r1.setRackName("R1"); r1.setZoneId(100L); r1.setWarehouseId(10L);
            Rack r2 = new Rack(); r2.setId(2L); r2.setRackName("R2"); r2.setZoneId(200L); r2.setWarehouseId(20L);
            resp.setRecords(Arrays.asList(r1, r2));
            resp.setTotal(2);
            return resp;
        });
        lenient().when(zoneMapper.selectById(any())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            Zone z = new Zone(); z.setId(id); z.setWarehouseId(id == 100L ? 10L : 20L);
            return z;
        });
        lenient().when(warehouseMapper.selectBatchIds(anyCollection())).thenReturn(Arrays.asList(
                buildWarehouse(10L, "W10"), buildWarehouse(20L, "W20")
        ));
        lenient().when(zoneMapper.selectBatchIds(anyCollection())).thenReturn(Arrays.asList(
                buildZone(100L, "Z100"), buildZone(200L, "Z200")
        ));
    }

    private Warehouse buildWarehouse(long id, String name) {
        Warehouse w = new Warehouse();
        w.setId(id); w.setWarehouseName(name);
        return w;
    }
    private Zone buildZone(long id, String name) {
        Zone z = new Zone(); z.setId(id); z.setZoneName(name);
        return z;
    }

    @Test
    void saveRack_autoFillWarehouse_unique_shouldSave() {
        RackServiceImpl spy = Mockito.spy(rackService);
        Rack r = new Rack(); r.setRackName("R1"); r.setRackCode("RC1"); r.setZoneId(100L);
        doReturn(true).when(spy).saveOrUpdate(any(Rack.class));
        boolean ok = spy.saveRack(r);
        Assertions.assertTrue(ok);
        Assertions.assertEquals(10L, r.getWarehouseId());
    }



    @Test
    void pageQuery_fillNames() {
        IPage<Rack> page = rackService.pageQuery(1, 10, null, null, null, null);
        Rack r = page.getRecords().get(0);
        Assertions.assertEquals("W10", r.getWarehouseName());
        Assertions.assertEquals("Z100", r.getZoneName());
    }
}

