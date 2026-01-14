package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxk.wms.wms_backend.entity.Category;
import com.lxk.wms.wms_backend.mapper.CategoryMapper;
import com.lxk.wms.wms_backend.service.impl.CategoryServiceImpl;
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
public class CategoryServiceImplTest {

    @Mock private CategoryMapper categoryMapper;
    @InjectMocks private CategoryServiceImpl categoryService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(categoryService, "baseMapper", categoryMapper);
        lenient().when(categoryMapper.selectCount(any())).thenReturn(0L);
        lenient().when(categoryMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<Category> req = invocation.getArgument(0);
            Page<Category> resp = new Page<>(req.getCurrent(), req.getSize());
            Category c1 = new Category(); c1.setId(1L); c1.setCategoryName("A");
            Category c2 = new Category(); c2.setId(2L); c2.setCategoryName("B");
            resp.setRecords(Arrays.asList(c1, c2));
            resp.setTotal(2);
            return resp;
        });
    }

    @Test
    void saveCategory_unique_shouldCallSave() {
        CategoryServiceImpl spy = Mockito.spy(categoryService);
        Category c = new Category();
        c.setCategoryName("A");
        c.setCategoryCode("C-A");
        doReturn(true).when(spy).saveOrUpdate(any(Category.class));
        boolean ok = spy.saveCategory(c);
        Assertions.assertTrue(ok);
    }

    @Test
    void saveCategory_duplicateName_shouldThrow() {
        Category c = new Category();
        c.setCategoryName("Dup");
        c.setCategoryCode("C-1");
        // first unique check hits count>0
        ReflectionTestUtils.setField(categoryService, "baseMapper", categoryMapper);
        Mockito.when(categoryMapper.selectCount(any())).thenReturn(1L);
        Assertions.assertThrows(RuntimeException.class, () -> categoryService.saveCategory(c));
    }

    @Test
    void pageQuery_basic() {
        IPage<Category> page = categoryService.pageQuery(1, 10, null, null);
        Assertions.assertEquals(2, page.getTotal());
        Assertions.assertEquals(2, page.getRecords().size());
    }
}

