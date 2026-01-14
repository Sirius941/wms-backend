package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxk.wms.wms_backend.entity.Unit;
import com.lxk.wms.wms_backend.mapper.UnitMapper;
import com.lxk.wms.wms_backend.service.impl.UnitServiceImpl;
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
public class UnitServiceImplTest {

    @Mock private UnitMapper unitMapper;
    @InjectMocks private UnitServiceImpl unitService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(unitService, "baseMapper", unitMapper);
        lenient().when(unitMapper.selectCount(any())).thenReturn(0L);
        lenient().when(unitMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<Unit> req = invocation.getArgument(0);
            Page<Unit> resp = new Page<>(req.getCurrent(), req.getSize());
            Unit u1 = new Unit(); u1.setId(1L); u1.setUnitName("箱");
            Unit u2 = new Unit(); u2.setId(2L); u2.setUnitName("托");
            resp.setRecords(Arrays.asList(u1, u2));
            resp.setTotal(2);
            return resp;
        });
    }

    @Test
    void saveUnit_unique_shouldCallSave() {
        UnitServiceImpl spy = Mockito.spy(unitService);
        Unit u = new Unit();
        u.setUnitName("箱");
        u.setUnitCode("BOX");
        doReturn(true).when(spy).saveOrUpdate(any(Unit.class));
        boolean ok = spy.saveUnit(u);
        Assertions.assertTrue(ok);
    }

    @Test
    void saveUnit_duplicateName_shouldThrow() {
        Unit u = new Unit();
        u.setUnitName("箱");
        u.setUnitCode("BOX");
        Mockito.when(unitMapper.selectCount(any())).thenReturn(1L);
        Assertions.assertThrows(RuntimeException.class, () -> unitService.saveUnit(u));
    }

    @Test
    void pageQuery_basic() {
        IPage<Unit> page = unitService.pageQuery(1, 10, null, null);
        Assertions.assertEquals(2, page.getTotal());
        Assertions.assertEquals(2, page.getRecords().size());
    }
}

