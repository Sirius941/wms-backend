package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxk.wms.wms_backend.entity.StorageType;
import com.lxk.wms.wms_backend.mapper.StorageTypeMapper;
import com.lxk.wms.wms_backend.service.impl.StorageTypeServiceImpl;
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
public class StorageTypeServiceImplTest {

    @Mock private StorageTypeMapper storageTypeMapper;
    @InjectMocks private StorageTypeServiceImpl storageTypeService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(storageTypeService, "baseMapper", storageTypeMapper);
        lenient().when(storageTypeMapper.selectCount(any())).thenReturn(0L);
        lenient().when(storageTypeMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<StorageType> req = invocation.getArgument(0);
            Page<StorageType> resp = new Page<>(req.getCurrent(), req.getSize());
            StorageType s1 = new StorageType(); s1.setId(1L); s1.setTypeName("常温");
            StorageType s2 = new StorageType(); s2.setId(2L); s2.setTypeName("冷藏");
            resp.setRecords(Arrays.asList(s1, s2));
            resp.setTotal(2);
            return resp;
        });
    }

    @Test
    void saveStorageType_unique_shouldCallSave() {
        StorageTypeServiceImpl spy = Mockito.spy(storageTypeService);
        StorageType s = new StorageType();
        s.setTypeName("常温");
        s.setTypeCode("NORMAL");
        doReturn(true).when(spy).saveOrUpdate(any(StorageType.class));
        boolean ok = spy.saveStorageType(s);
        Assertions.assertTrue(ok);
    }

    @Test
    void saveStorageType_duplicateName_shouldThrow() {
        StorageType s = new StorageType();
        s.setTypeName("常温");
        s.setTypeCode("NORMAL");
        Mockito.when(storageTypeMapper.selectCount(any())).thenReturn(1L);
        Assertions.assertThrows(RuntimeException.class, () -> storageTypeService.saveStorageType(s));
    }

    @Test
    void pageQuery_basic() {
        IPage<StorageType> page = storageTypeService.pageQuery(1, 10, null, null);
        Assertions.assertEquals(2, page.getTotal());
        Assertions.assertEquals(2, page.getRecords().size());
    }
}

