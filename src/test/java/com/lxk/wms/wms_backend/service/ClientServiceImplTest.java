package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxk.wms.wms_backend.entity.Client;
import com.lxk.wms.wms_backend.mapper.ClientMapper;
import com.lxk.wms.wms_backend.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ClientServiceImplTest {

    @Mock private ClientMapper clientMapper;
    @InjectMocks private ClientServiceImpl clientService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(clientService, "baseMapper", clientMapper);
        lenient().when(clientMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<Client> req = invocation.getArgument(0);
            Page<Client> resp = new Page<>(req.getCurrent(), req.getSize());
            Client c1 = new Client(); c1.setId(1L); c1.setClientName("C1"); c1.setClientType(1);
            Client c2 = new Client(); c2.setId(2L); c2.setClientName("C2"); c2.setClientType(2);
            resp.setRecords(Arrays.asList(c1, c2));
            resp.setTotal(2);
            return resp;
        });
    }

    @Test
    void pageQuery_basic() {
        IPage<Client> page = clientService.pageQuery(1, 10, null, null);
        Assertions.assertEquals(2, page.getTotal());
        Assertions.assertEquals(2, page.getRecords().size());
    }
}

