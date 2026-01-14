package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxk.wms.wms_backend.entity.Bin;
import com.lxk.wms.wms_backend.entity.Inbound;
import com.lxk.wms.wms_backend.entity.InboundDetail;
import com.lxk.wms.wms_backend.entity.Product;
import com.lxk.wms.wms_backend.entity.ReceiveTask;
import com.lxk.wms.wms_backend.mapper.BinMapper;
import com.lxk.wms.wms_backend.mapper.InboundDetailMapper;
import com.lxk.wms.wms_backend.mapper.InboundMapper;
import com.lxk.wms.wms_backend.mapper.ReceiveTaskMapper;
import com.lxk.wms.wms_backend.mapper.ProductMapper;
import com.lxk.wms.wms_backend.mapper.WarehouseMapper;
import com.lxk.wms.wms_backend.service.impl.ReceiveTaskServiceImpl;
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
public class ReceiveTaskServiceImplTest {

    @Mock private ReceiveTaskMapper receiveTaskMapper;
    @Mock private InboundDetailMapper inboundDetailMapper;
    @Mock private InboundMapper inboundMapper;
    @Mock private ProductMapper productMapper;
    @Mock private WarehouseMapper warehouseMapper;
    @Mock private BinMapper binMapper;
    @Mock private InventoryService inventoryService;

    @InjectMocks private ReceiveTaskServiceImpl receiveTaskService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(receiveTaskService, "baseMapper", receiveTaskMapper);
        lenient().when(receiveTaskMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<ReceiveTask> req = invocation.getArgument(0);
            Page<ReceiveTask> resp = new Page<>(req.getCurrent(), req.getSize());
            ReceiveTask t1 = new ReceiveTask(); t1.setId(1L); t1.setInboundId(200L); t1.setProductId(1001L); t1.setBinId(31L);
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
        lenient().when(inboundMapper.selectBatchIds(anyCollection())).thenReturn(Arrays.asList(
                buildInbound(200L, "IN200")
        ));
        lenient().when(binMapper.selectById(any())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            Bin b = new Bin(); b.setId(id); b.setRackId(1000L); b.setZoneId(100L); b.setWarehouseId(10L);
            return b;
        });
        lenient().doReturn(null).when(warehouseMapper).selectById(any());
        // Use doNothing for void method to avoid generics issues
        Mockito.doNothing().when(inventoryService).addInventory(any(), any(), any(), any(), any(), any(), any());
        lenient().when(inboundDetailMapper.selectById(any())).thenReturn(buildInboundDetail(1L, 5));
    }

    private Product buildProduct(long id, String name, String sku) { Product p = new Product(); p.setId(id); p.setProductName(name); p.setSku(sku); return p; }
    private Bin buildBin(long id, String code) { Bin b = new Bin(); b.setId(id); b.setBinCode(code); return b; }
    private Inbound buildInbound(long id, String no) { Inbound o = new Inbound(); o.setId(id); o.setInboundNo(no); return o; }
    private InboundDetail buildInboundDetail(long id, int realQty) { InboundDetail d = new InboundDetail(); d.setId(id); d.setRealQuantity(realQty); return d; }

    @Test
    void createTasks_autoFill_shouldSave() {
        ReceiveTaskServiceImpl spy = Mockito.spy(receiveTaskService);
        doReturn(true).when(spy).save(any(ReceiveTask.class));
        ReceiveTask t = new ReceiveTask(); t.setProductId(1001L); t.setBinId(31L); t.setQuantity(5);
        spy.createTasks(t);
        Assertions.assertEquals(0, t.getStatus());
        Assertions.assertEquals(10L, t.getWarehouseId());
    }

    @Test
    void completeTask_shouldAddInventoryAndUpdate() {
        ReceiveTaskServiceImpl spy = Mockito.spy(receiveTaskService);
        ReceiveTask t = new ReceiveTask(); t.setId(1L); t.setProductId(1001L); t.setBinId(31L); t.setQuantity(5); t.setStatus(0); t.setInboundDetailId(1L); t.setInboundId(200L);
        doReturn(t).when(spy).getById(any());
        doReturn(true).when(spy).updateById(any(ReceiveTask.class));
        // Specify exact type to avoid ambiguous method reference
        Mockito.when(inboundDetailMapper.updateById(any(InboundDetail.class))).thenReturn(1);
        spy.completeTask(1L);
        Assertions.assertEquals(1, t.getStatus());
    }

    @Test
    void pageQuery_fillNames() {
        IPage<ReceiveTask> page = receiveTaskService.pageQuery(1, 10, null, null);
        ReceiveTask t = page.getRecords().get(0);
        Assertions.assertEquals("P1001", t.getProductName());
        Assertions.assertEquals("B31", t.getBinCode());
        Assertions.assertEquals("IN200", t.getInboundNo());
    }
}

