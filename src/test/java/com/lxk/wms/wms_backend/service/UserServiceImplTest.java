package com.lxk.wms.wms_backend.service;

import com.lxk.wms.wms_backend.entity.User;
import com.lxk.wms.wms_backend.mapper.UserMapper;
import com.lxk.wms.wms_backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceImplTest {

    @Mock private UserMapper userMapper;
    @InjectMocks private UserServiceImpl userService;

    @Test
    void saveUser_defaultPasswordOnCreate() {
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
        UserServiceImpl spyService = Mockito.spy(userService);
        User u = new User();
        u.setUsername("tom");
        // simulate no duplicate username
        when(userMapper.selectCount(any())).thenReturn(0L);
        doReturn(true).when(spyService).saveOrUpdate(any(User.class));
        boolean ok = spyService.saveUser(u);
        Assertions.assertTrue(ok);
        Assertions.assertEquals("123456", u.getPassword());
    }

    @Test
    void saveUser_duplicateUsername_shouldThrow() {
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
        User u = new User();
        u.setId(1L);
        u.setUsername("tom");
        // count > 0 triggers duplicate
        when(userMapper.selectCount(any())).thenReturn(1L);
        Assertions.assertThrows(RuntimeException.class, () -> userService.saveUser(u));
    }
}
