package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxk.wms.wms_backend.entity.Category;
import com.lxk.wms.wms_backend.entity.Product;
import com.lxk.wms.wms_backend.entity.StorageType;
import com.lxk.wms.wms_backend.entity.Unit;
import com.lxk.wms.wms_backend.mapper.CategoryMapper;
import com.lxk.wms.wms_backend.mapper.ProductMapper;
import com.lxk.wms.wms_backend.mapper.StorageTypeMapper;
import com.lxk.wms.wms_backend.mapper.UnitMapper;
import com.lxk.wms.wms_backend.service.impl.ProductServiceImpl;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProductServiceImplTest {

    @Mock private ProductMapper productMapper;
    @Mock private CategoryMapper categoryMapper;
    @Mock private UnitMapper unitMapper;
    @Mock private StorageTypeMapper storageTypeMapper;

    @InjectMocks private ProductServiceImpl productService;

    private Product buildProduct(long id, long catId, long unitId, long typeId, String name, String sku) {
        Product p = new Product();
        p.setId(id);
        p.setCategoryId(catId);
        p.setUnitId(unitId);
        p.setStorageTypeId(typeId);
        p.setProductName(name);
        p.setSku(sku);
        return p;
    }

    private Category buildCategory(long id, String name) {
        Category c = new Category();
        c.setId(id);
        c.setCategoryName(name);
        return c;
    }

    private Unit buildUnit(long id, String name) {
        Unit u = new Unit();
        u.setId(id);
        u.setUnitName(name);
        return u;
    }

    private StorageType buildStorageType(long id, String name) {
        StorageType s = new StorageType();
        s.setId(id);
        s.setTypeName(name);
        return s;
    }

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(productService, "baseMapper", productMapper);

        lenient().when(productMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<Product> req = invocation.getArgument(0);
            Page<Product> resp = new Page<>(req.getCurrent(), req.getSize());
            List<Product> data = Arrays.asList(
                    buildProduct(1L, 10L, 100L, 1000L, "A", "SKU-A"),
                    buildProduct(2L, 20L, 200L, 2000L, "B", "SKU-B")
            );
            resp.setRecords(data);
            resp.setTotal(data.size());
            return resp;
        });
        lenient().when(productMapper.selectCount(any())).thenReturn(0L);

        lenient().when(categoryMapper.selectBatchIds(anyCollection())).thenReturn(Arrays.asList(
                buildCategory(10L, "Cat-10"), buildCategory(20L, "Cat-20")
        ));
        lenient().when(unitMapper.selectBatchIds(anyCollection())).thenReturn(Arrays.asList(
                buildUnit(100L, "Unit-100"), buildUnit(200L, "Unit-200")
        ));
        lenient().when(storageTypeMapper.selectBatchIds(anyCollection())).thenReturn(Arrays.asList(
            buildStorageType(1000L, "Type-1000"), buildStorageType(2000L, "Type-2000")
        ));
    }

    @Test
    void saveProduct_uniqueSku_shouldSave() {
        ProductServiceImpl spyService = Mockito.spy(productService);
        Product product = new Product();
        product.setProductName("A");
        product.setSku("SKU-A");
        product.setCategoryId(10L);
        product.setUnitId(100L);
        product.setStorageTypeId(1000L);
        doReturn(true).when(spyService).saveOrUpdate(any(Product.class));
        boolean ok = spyService.saveProduct(product);
        Assertions.assertTrue(ok);
    }

    @Test
    void pageQuery_shouldFillNames() {
        IPage<Product> page = productService.pageQuery(1, 10, null, null, null);
        List<Product> records = page.getRecords();
        Assertions.assertEquals(2, records.size());
        Product p1 = records.get(0);
        Assertions.assertEquals("Cat-10", p1.getCategoryName());
        Assertions.assertEquals("Unit-100", p1.getUnitName());
        Assertions.assertEquals("Type-1000", p1.getStorageTypeName());
    }

    @Test
    void pageQuery_emptyRecords_safe() {
        lenient().when(productMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<Product> req = invocation.getArgument(0);
            Page<Product> resp = new Page<>(req.getCurrent(), req.getSize());
            resp.setRecords(Collections.emptyList());
            resp.setTotal(0);
            return resp;
        });
        IPage<Product> page = productService.pageQuery(1, 10, null, null, null);
        Assertions.assertTrue(page.getRecords().isEmpty());
    }
}
