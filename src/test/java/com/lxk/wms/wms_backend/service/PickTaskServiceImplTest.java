package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxk.wms.wms_backend.entity.Bin;
import com.lxk.wms.wms_backend.entity.Outbound;
import com.lxk.wms.wms_backend.entity.OutboundDetail;
import com.lxk.wms.wms_backend.entity.PickTask;
import com.lxk.wms.wms_backend.entity.Product;
import com.lxk.wms.wms_backend.mapper.BinMapper;
import com.lxk.wms.wms_backend.mapper.OutboundDetailMapper;
import com.lxk.wms.wms_backend.mapper.OutboundMapper;
import com.lxk.wms.wms_backend.mapper.PickTaskMapper;
import com.lxk.wms.wms_backend.mapper.ProductMapper;
import com.lxk.wms.wms_backend.mapper.WarehouseMapper;
import com.lxk.wms.wms_backend.service.impl.PickTaskServiceImpl;
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
public class PickTaskServiceImplTest {

    @Mock private PickTaskMapper pickTaskMapper;
    @Mock private OutboundDetailMapper outboundDetailMapper;
    @Mock private OutboundMapper outboundMapper;
    @Mock private ProductMapper productMapper;
    @Mock private WarehouseMapper warehouseMapper;
    @Mock private BinMapper binMapper;
    @Mock private InventoryService inventoryService;

    @InjectMocks private PickTaskServiceImpl pickTaskService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(pickTaskService, "baseMapper", pickTaskMapper);
        lenient().when(pickTaskMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<PickTask> req = invocation.getArgument(0);
            Page<PickTask> resp = new Page<>(req.getCurrent(), req.getSize());
            PickTask t1 = new PickTask(); t1.setId(1L); t1.setOutboundId(100L); t1.setProductId(1001L); t1.setBinId(31L);
            resp.setRecords(Arrays.asList(t1));
            resp.setTotal(1);
            return resp;
        });
        lenient().when(productMapper.selectBatchIds(anyCollection())).thenReturn(Arrays.asList(
                buildProduct(1001L, "P1001", "SKU1001")
        ));
        lenient().when(binMapper.selectBatchIds(anyCollection())).thenReturn(Arrays.asList(
                buildBin(31L, "B31")
        ));
        lenient().when(outboundMapper.selectBatchIds(anyCollection())).thenReturn(Arrays.asList(
                buildOutbound(100L, "OUT100")
        ));
        lenient().when(binMapper.selectById(any())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            Bin b = new Bin(); b.setId(id); b.setRackId(1000L); b.setZoneId(100L); b.setWarehouseId(10L);
            return b;
        });
        lenient().when(inventoryService.getStockQuantity(any(), any())).thenReturn(10);
        lenient().when(outboundDetailMapper.selectById(any())).thenReturn(buildOutboundDetail(1L, 5));
    }

    private Product buildProduct(long id, String name, String sku) { Product p = new Product(); p.setId(id); p.setProductName(name); p.setSku(sku); return p; }
    private Bin buildBin(long id, String code) { Bin b = new Bin(); b.setId(id); b.setBinCode(code); return b; }
    private Outbound buildOutbound(long id, String no) { Outbound o = new Outbound(); o.setId(id); o.setOutboundNo(no); return o; }
    private OutboundDetail buildOutboundDetail(long id, int realQty) { OutboundDetail d = new OutboundDetail(); d.setId(id); d.setRealQuantity(realQty); return d; }

    @Test
    void createTask_autoFillAndStockCheck_shouldSave() {
        PickTaskServiceImpl spy = Mockito.spy(pickTaskService);
        doReturn(true).when(spy).save(any(PickTask.class));
        PickTask t = new PickTask(); t.setProductId(1001L); t.setBinId(31L); t.setQuantity(5);
        spy.createTask(t);
        Assertions.assertEquals(0, t.getStatus());
        Assertions.assertEquals(10L, t.getWarehouseId());
    }

    @Test
    void completeTask_shouldDeductStockAndUpdate() {
        PickTaskServiceImpl spy = Mockito.spy(pickTaskService);
        PickTask t = new PickTask(); t.setId(1L); t.setProductId(1001L); t.setBinId(31L); t.setQuantity(5); t.setStatus(0); t.setOutboundDetailId(1L);
        doReturn(t).when(spy).getById(any());
        Mockito.when(inventoryService.deductInventory(any(), any(), any())).thenReturn(true);
        doReturn(true).when(spy).updateById(any(PickTask.class));
        Mockito.when(outboundDetailMapper.updateById((OutboundDetail) any())).thenReturn(1);
        spy.completeTask(1L);
        Assertions.assertEquals(1, t.getStatus());
    }

    @Test
    void pageQuery_fillNames() {
        IPage<PickTask> page = pickTaskService.pageQuery(1, 10, null, null);
        PickTask t = page.getRecords().get(0);
        Assertions.assertEquals("P1001", t.getProductName());
        Assertions.assertEquals("B31", t.getBinCode());
        Assertions.assertEquals("OUT100", t.getOutboundNo());
    }
}
