package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxk.wms.wms_backend.entity.Bin;
import com.lxk.wms.wms_backend.entity.Rack;
import com.lxk.wms.wms_backend.entity.Zone;
import com.lxk.wms.wms_backend.entity.Warehouse;
import com.lxk.wms.wms_backend.mapper.BinMapper;
import com.lxk.wms.wms_backend.mapper.RackMapper;
import com.lxk.wms.wms_backend.mapper.ZoneMapper;
import com.lxk.wms.wms_backend.mapper.WarehouseMapper;
import com.lxk.wms.wms_backend.service.impl.BinServiceImpl;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BinServiceImplTest {

    @Mock private BinMapper binMapper;
    @Mock private RackMapper rackMapper;
    @Mock private ZoneMapper zoneMapper;
    @Mock private WarehouseMapper warehouseMapper;
    @InjectMocks private BinServiceImpl binService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(binService, "baseMapper", binMapper);
        lenient().when(binMapper.selectCount(any())).thenReturn(0L);
        lenient().when(binMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<Bin> req = invocation.getArgument(0);
            Page<Bin> resp = new Page<>(req.getCurrent(), req.getSize());
            Bin b1 = new Bin(); b1.setId(1L); b1.setBinCode("B1"); b1.setRackId(1000L); b1.setZoneId(100L); b1.setWarehouseId(10L);
            resp.setRecords(Arrays.asList(b1));
            resp.setTotal(1);
            return resp;
        });
        lenient().when(rackMapper.selectById(any())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            Rack r = new Rack(); r.setId(id); r.setZoneId(100L); r.setWarehouseId(10L);
            return r;
        });
        lenient().when(warehouseMapper.selectBatchIds(anyCollection())).thenReturn(Arrays.asList(
                buildWarehouse(10L, "W10")
        ));
        lenient().when(zoneMapper.selectBatchIds(anyCollection())).thenReturn(Arrays.asList(
                buildZone(100L, "Z100")
        ));
        lenient().when(rackMapper.selectBatchIds(anyCollection())).thenReturn(Arrays.asList(
                buildRack(1000L, "R1000")
        ));
    }

    private Warehouse buildWarehouse(long id, String name) { Warehouse w = new Warehouse(); w.setId(id); w.setWarehouseName(name); return w; }
    private Zone buildZone(long id, String name) { Zone z = new Zone(); z.setId(id); z.setZoneName(name); return z; }
    private Rack buildRack(long id, String name) { Rack r = new Rack(); r.setId(id); r.setRackName(name); return r; }

    @Test
    void saveBin_autoFillAndDefaultUsage_shouldSave() {
        BinServiceImpl spy = Mockito.spy(binService);
        Bin b = new Bin(); b.setBinCode("B1"); b.setRackId(1000L);
        doReturn(true).when(spy).saveOrUpdate(any(Bin.class));
        boolean ok = spy.saveBin(b);
        Assertions.assertTrue(ok);
        Assertions.assertEquals(0, b.getUsageStatus());
        Assertions.assertEquals(100L, b.getZoneId());
        Assertions.assertEquals(10L, b.getWarehouseId());
    }

    @Test
    void saveBin_missingRack_shouldThrow() {
        Mockito.when(rackMapper.selectById(any())).thenReturn(null);
        Bin b = new Bin(); b.setBinCode("B1"); b.setRackId(9999L);
        Assertions.assertThrows(RuntimeException.class, () -> binService.saveBin(b));
    }

    @Test
    void pageQuery_fillNames() {
        IPage<Bin> page = binService.pageQuery(1, 10, null, null, null, null, null, null);
        Bin b = page.getRecords().get(0);
        Assertions.assertEquals("W10", b.getWarehouseName());
        Assertions.assertEquals("Z100", b.getZoneName());
        Assertions.assertEquals("R1000", b.getRackName());
    }
}

