package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxk.wms.wms_backend.dto.OutboundDTO;
import com.lxk.wms.wms_backend.entity.Outbound;
import com.lxk.wms.wms_backend.entity.OutboundDetail;
import com.lxk.wms.wms_backend.entity.Product;
import com.lxk.wms.wms_backend.entity.Warehouse;
import com.lxk.wms.wms_backend.mapper.OutboundDetailMapper;
import com.lxk.wms.wms_backend.mapper.OutboundMapper;
import com.lxk.wms.wms_backend.mapper.ProductMapper;
import com.lxk.wms.wms_backend.mapper.WarehouseMapper;
import com.lxk.wms.wms_backend.service.impl.OutboundServiceImpl;
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
public class OutboundServiceImplTest {

    @Mock private OutboundMapper outboundMapper;
    @Mock private OutboundDetailMapper outboundDetailMapper;
    @Mock private WarehouseMapper warehouseMapper;
    @Mock private ProductMapper productMapper;
    @InjectMocks private OutboundServiceImpl outboundService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(outboundService, "baseMapper", outboundMapper);
        lenient().when(outboundMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<Outbound> req = invocation.getArgument(0);
            Page<Outbound> resp = new Page<>(req.getCurrent(), req.getSize());
            Outbound o1 = new Outbound(); o1.setId(1L); o1.setWarehouseId(10L);
            resp.setRecords(Arrays.asList(o1));
            resp.setTotal(1);
            return resp;
        });
        lenient().when(warehouseMapper.selectById(any())).thenReturn(buildWarehouse(10L, "W10"));
        lenient().when(outboundDetailMapper.selectList(any())).thenReturn(Arrays.asList(
                buildDetail(201L, 2001L, 5)
        ));
        lenient().when(productMapper.selectBatchIds(anyCollection())).thenReturn(Arrays.asList(
                buildProduct(2001L, "P2001", "SKU2001", "IMG")
        ));
    }

    private Warehouse buildWarehouse(long id, String name) { Warehouse w = new Warehouse(); w.setId(id); w.setWarehouseName(name); return w; }
    private OutboundDetail buildDetail(long id, long productId, int qty) { OutboundDetail d = new OutboundDetail(); d.setId(id); d.setProductId(productId); d.setPlanQuantity(qty); return d; }
    private Product buildProduct(long id, String name, String sku, String img) { Product p = new Product(); p.setId(id); p.setProductName(name); p.setSku(sku); p.setImgUrl(img); return p; }

    @Test
    void saveOutbound_insertFlow_shouldSaveMainAndDetails() {
        OutboundServiceImpl spy = Mockito.spy(outboundService);
        doReturn(true).when(spy).save(any(Outbound.class));
        OutboundDTO dto = new OutboundDTO();
        OutboundDetail d = new OutboundDetail(); d.setProductId(2001L); d.setPlanQuantity(5);
        dto.setProducts(Collections.singletonList(d));
        spy.saveOutbound(dto);
        Assertions.assertEquals(0, dto.getProducts().get(0).getRealQuantity());
    }

    @Test
    void pageQuery_fillWarehouseName() {
        IPage<Outbound> page = outboundService.pageQuery(1, 10, null, null);
        Outbound o = page.getRecords().get(0);
        Assertions.assertEquals("W10", o.getWarehouseName());
    }

    @Test
    void getDetailById_fillsDetailsAndProductInfo() {
        OutboundServiceImpl spy = Mockito.spy(outboundService);
        doReturn(buildOutbound(1L)).when(spy).getById(any());
        Outbound outbound = spy.getDetailById(1L);
        Assertions.assertFalse(outbound.getDetails().isEmpty());
        OutboundDetail d = outbound.getDetails().get(0);
        Assertions.assertEquals("P2001", d.getProductName());
        Assertions.assertEquals("SKU2001", d.getProductSku());
        Assertions.assertEquals("IMG", d.getProductImg());
    }

    private Outbound buildOutbound(long id) { Outbound o = new Outbound(); o.setId(id); o.setWarehouseId(10L); return o; }
}
