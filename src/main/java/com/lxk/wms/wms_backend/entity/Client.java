package com.lxk.wms.wms_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wms_client")
public class Client {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String clientName;
    private String clientCode;
    private Integer clientType; // 1-供应商, 2-客户
    private String contactPerson;
    private String phone;
    private String address;
    private Integer status;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}