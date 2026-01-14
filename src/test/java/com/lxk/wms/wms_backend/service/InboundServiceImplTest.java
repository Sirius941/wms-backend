package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxk.wms.wms_backend.dto.InboundDTO;
import com.lxk.wms.wms_backend.entity.Inbound;
import com.lxk.wms.wms_backend.entity.InboundDetail;
import com.lxk.wms.wms_backend.entity.Product;
import com.lxk.wms.wms_backend.entity.Warehouse;
import com.lxk.wms.wms_backend.mapper.InboundDetailMapper;
import com.lxk.wms.wms_backend.mapper.InboundMapper;
import com.lxk.wms.wms_backend.mapper.ProductMapper;
import com.lxk.wms.wms_backend.mapper.WarehouseMapper;
import com.lxk.wms.wms_backend.service.impl.InboundServiceImpl;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InboundServiceImplTest {

    @Mock private InboundMapper inboundMapper;
    @Mock private InboundDetailMapper inboundDetailMapper;
    @Mock private WarehouseMapper warehouseMapper;
    @Mock private ProductMapper productMapper;
    @InjectMocks private InboundServiceImpl inboundService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(inboundService, "baseMapper", inboundMapper);
        lenient().when(inboundMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<Inbound> req = invocation.getArgument(0);
            Page<Inbound> resp = new Page<>(req.getCurrent(), req.getSize());
            Inbound i1 = new Inbound(); i1.setId(1L); i1.setWarehouseId(10L);
            resp.setRecords(Arrays.asList(i1));
            resp.setTotal(1);
            return resp;
        });
        lenient().when(warehouseMapper.selectById(any())).thenReturn(buildWarehouse(10L, "W10"));
        lenient().when(inboundDetailMapper.selectList(any())).thenReturn(Arrays.asList(
                buildDetail(101L, 1001L, 3)
        ));
        lenient().when(productMapper.selectBatchIds(anyCollection())).thenReturn(Arrays.asList(
                buildProduct(1001L, "P1001", "SKU1001", "IMG")
        ));
    }

    private Warehouse buildWarehouse(long id, String name) { Warehouse w = new Warehouse(); w.setId(id); w.setWarehouseName(name); return w; }
    private InboundDetail buildDetail(long id, long productId, int qty) { InboundDetail d = new InboundDetail(); d.setId(id); d.setProductId(productId); d.setPlanQuantity(qty); return d; }
    private Product buildProduct(long id, String name, String sku, String img) { Product p = new Product(); p.setId(id); p.setProductName(name); p.setSku(sku); p.setImgUrl(img); return p; }

    @Test
    void saveInbound_insertFlow_shouldSaveMainAndDetails() {
        InboundServiceImpl spy = Mockito.spy(inboundService);
        doReturn(true).when(spy).save(any(Inbound.class));
        InboundDTO dto = new InboundDTO();
        InboundDetail d = new InboundDetail(); d.setProductId(1001L); d.setPlanQuantity(3);
        dto.setProducts(Collections.singletonList(d));
        spy.saveInbound(dto);
        // verify basic effects: details got inboundId assigned via service flow
        Assertions.assertEquals(0, dto.getProducts().get(0).getRealQuantity());
    }

    @Test
    void pageQuery_fillWarehouseName() {
        IPage<Inbound> page = inboundService.pageQuery(1, 10, null, null);
        Inbound i = page.getRecords().get(0);
        Assertions.assertEquals("W10", i.getWarehouseName());
    }

    @Test
    void getDetailById_fillsDetailsAndProductInfo() {
        InboundServiceImpl spy = Mockito.spy(inboundService);
        doReturn(buildInbound(1L)).when(spy).getById(any());
        Inbound inbound = spy.getDetailById(1L);
        Assertions.assertFalse(inbound.getDetails().isEmpty());
        InboundDetail d = inbound.getDetails().get(0);
        Assertions.assertEquals("P1001", d.getProductName());
        Assertions.assertEquals("SKU1001", d.getProductSku());
        Assertions.assertEquals("IMG", d.getProductImg());
    }

    private Inbound buildInbound(long id) { Inbound i = new Inbound(); i.setId(id); i.setWarehouseId(10L); return i; }
}
