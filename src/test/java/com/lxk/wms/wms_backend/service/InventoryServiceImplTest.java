package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxk.wms.wms_backend.entity.Bin;
import com.lxk.wms.wms_backend.entity.Inventory;
import com.lxk.wms.wms_backend.entity.Product;
import com.lxk.wms.wms_backend.entity.Unit;
import com.lxk.wms.wms_backend.entity.Warehouse;
import com.lxk.wms.wms_backend.mapper.BinMapper;
import com.lxk.wms.wms_backend.mapper.InventoryMapper;
import com.lxk.wms.wms_backend.mapper.ProductMapper;
import com.lxk.wms.wms_backend.mapper.UnitMapper;
import com.lxk.wms.wms_backend.mapper.WarehouseMapper;
import com.lxk.wms.wms_backend.service.impl.InventoryServiceImpl;
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
public class InventoryServiceImplTest {

    @Mock private InventoryMapper inventoryMapper;
    @Mock private ProductMapper productMapper;
    @Mock private WarehouseMapper warehouseMapper;
    @Mock private BinMapper binMapper;
    @Mock private UnitMapper unitMapper;

    @InjectMocks private InventoryServiceImpl inventoryService;

    private Inventory inv(long id, long productId, long warehouseId, long binId, int qty) {
        Inventory i = new Inventory();
        i.setId(id);
        i.setProductId(productId);
        i.setWarehouseId(warehouseId);
        i.setBinId(binId);
        i.setQuantity(qty);
        return i;
    }

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(inventoryService, "baseMapper", inventoryMapper);

        lenient().when(inventoryMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<Inventory> req = invocation.getArgument(0);
            Page<Inventory> resp = new Page<>(req.getCurrent(), req.getSize());
            List<Inventory> data = Arrays.asList(
                    inv(1L, 11L, 21L, 31L, 5),
                    inv(2L, 12L, 22L, 32L, 10)
            );
            resp.setRecords(data);
            resp.setTotal(data.size());
            return resp;
        });

        // batch lookups
        lenient().when(productMapper.selectBatchIds(anyCollection())).thenReturn(Arrays.asList(
                buildProduct(11L, "P11", 101L, "SKU11"),
                buildProduct(12L, "P12", 102L, "SKU12")
        ));
        lenient().when(unitMapper.selectBatchIds(anyCollection())).thenReturn(Arrays.asList(
                buildUnit(101L, "U101"), buildUnit(102L, "U102")
        ));
        lenient().when(warehouseMapper.selectBatchIds(anyCollection())).thenReturn(Arrays.asList(
                buildWarehouse(21L, "W21"), buildWarehouse(22L, "W22")
        ));
        lenient().when(binMapper.selectBatchIds(anyCollection())).thenReturn(Arrays.asList(
                buildBin(31L, "B31"), buildBin(32L, "B32")
        ));
    }

    private Product buildProduct(long id, String name, long unitId, String sku) {
        Product p = new Product();
        p.setId(id);
        p.setProductName(name);
        p.setUnitId(unitId);
        p.setSku(sku);
        return p;
    }
    private Unit buildUnit(long id, String name) {
        Unit u = new Unit();
        u.setId(id);
        u.setUnitName(name);
        return u;
    }
    private Warehouse buildWarehouse(long id, String name) {
        Warehouse w = new Warehouse();
        w.setId(id);
        w.setWarehouseName(name);
        return w;
    }
    private Bin buildBin(long id, String code) {
        Bin b = new Bin();
        b.setId(id);
        b.setBinCode(code);
        return b;
    }

    @Test
    void pageQuery_shouldFillMappingFields() {
        IPage<Inventory> page = inventoryService.pageQuery(1, 10, null, null);
        Inventory i = page.getRecords().get(0);
        Assertions.assertEquals("P11", i.getProductName());
        Assertions.assertEquals("SKU11", i.getProductSku());
        Assertions.assertEquals("U101", i.getUnitName());
        Assertions.assertEquals("W21", i.getWarehouseName());
        Assertions.assertEquals("B31", i.getBinCode());
    }

    @Test
    void deductInventory_shouldSucceed() {
        InventoryServiceImpl spy = Mockito.spy(inventoryService);
        doReturn(inv(3L, 11L, 21L, 31L, 10)).when(spy).getOne(any());
        doReturn(true).when(spy).updateById(any(Inventory.class));
        boolean ok = spy.deductInventory(11L, 31L, 5);
        Assertions.assertTrue(ok);
    }

    @Test
    void getStockQuantity_returnsValue() {
        InventoryServiceImpl spy = Mockito.spy(inventoryService);
        doReturn(inv(3L, 11L, 21L, 31L, 10)).when(spy).getOne(any());
        Integer q = spy.getStockQuantity(11L, 31L);
        Assertions.assertEquals(10, q);
    }
}
