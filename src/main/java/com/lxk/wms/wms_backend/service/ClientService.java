package com.lxk.wms.wms_backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lxk.wms.wms_backend.entity.Client;

public interface ClientService extends IService<Client> {
    boolean saveClient(Client client);

    IPage<Client> pageQuery(Integer pageNum, Integer pageSize, String name, Integer type);
}

