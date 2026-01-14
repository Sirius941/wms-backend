package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxk.wms.wms_backend.entity.ProductTag;
import com.lxk.wms.wms_backend.mapper.ProductTagMapper;
import com.lxk.wms.wms_backend.service.impl.ProductTagServiceImpl;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProductTagServiceImplTest {

    @Mock private ProductTagMapper productTagMapper;
    @InjectMocks private ProductTagServiceImpl productTagService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(productTagService, "baseMapper", productTagMapper);
        lenient().when(productTagMapper.selectCount(any())).thenReturn(0L);
        lenient().when(productTagMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<ProductTag> req = invocation.getArgument(0);
            Page<ProductTag> resp = new Page<>(req.getCurrent(), req.getSize());
            ProductTag t1 = new ProductTag(); t1.setId(1L); t1.setTagName("Hot");
            ProductTag t2 = new ProductTag(); t2.setId(2L); t2.setTagName("New");
            resp.setRecords(Arrays.asList(t1, t2));
            resp.setTotal(2);
            return resp;
        });
    }

    @Test
    void saveProductTag_uniqueAndDefaultColor_shouldSave() {
        ProductTagServiceImpl spy = Mockito.spy(productTagService);
        ProductTag t = new ProductTag(); t.setTagName("Hot"); t.setTagCode("HOT");
        doReturn(true).when(spy).saveOrUpdate(any(ProductTag.class));
        boolean ok = spy.saveProductTag(t);
        Assertions.assertTrue(ok);
        Assertions.assertEquals("#409EFF", t.getColor());
    }

    @Test
    void saveProductTag_duplicate_shouldThrow() {
        ProductTag t = new ProductTag(); t.setTagName("Hot"); t.setTagCode("HOT"); t.setColor("#000000");
        Mockito.when(productTagMapper.selectCount(any())).thenReturn(1L);
        Assertions.assertThrows(RuntimeException.class, () -> productTagService.saveProductTag(t));
    }

    @Test
    void pageQuery_basic() {
        IPage<ProductTag> page = productTagService.pageQuery(1, 10, null, null);
        Assertions.assertEquals(2, page.getTotal());
        Assertions.assertEquals(2, page.getRecords().size());
    }
}

