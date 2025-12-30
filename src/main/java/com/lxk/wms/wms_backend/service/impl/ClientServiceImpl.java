package com.lxk.wms.wms_backend.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxk.wms.wms_backend.entity.Client;
import com.lxk.wms.wms_backend.mapper.ClientMapper;
import com.lxk.wms.wms_backend.service.ClientService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ClientServiceImpl extends ServiceImpl<ClientMapper, Client> implements ClientService {
    @Override
    public boolean saveClient(Client client) {
        return false;
    }

    @Override
    public IPage<Client> pageQuery(Integer pageNum, Integer pageSize, String name, Integer type) {
        LambdaQueryWrapper<Client> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) wrapper.like(Client::getClientName, name);
        if (type != null) wrapper.eq(Client::getClientType, type);
        wrapper.orderByDesc(Client::getCreateTime);
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }
}