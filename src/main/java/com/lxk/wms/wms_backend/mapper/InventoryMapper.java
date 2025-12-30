package com.lxk.wms.wms_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lxk.wms.wms_backend.dto.InventorySummaryDTO;
import com.lxk.wms.wms_backend.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    // ✅ 新增：库存汇总查询 (按产品分组)
    @Select("SELECT " +
            "i.product_id, " +
            "MAX(p.product_name) as product_name, " +
            "MAX(p.sku) as product_sku, " +
            "MAX(p.img_url) as img_url, " +
            "MAX(c.category_name) as category_name, " +
            "MAX(u.unit_name) as unit_name, " +
            "SUM(i.quantity) as total_quantity, " +
            "SUM(i.locked_quantity) as total_locked " +
            "FROM wms_inventory i " +
            "LEFT JOIN wms_product p ON i.product_id = p.id " +
            "LEFT JOIN wms_category c ON p.category_id = c.id " +
            "LEFT JOIN wms_unit u ON p.unit_id = u.id " +
            "WHERE i.is_deleted = 0 " +
            "AND (p.product_name LIKE CONCAT('%', #{keyword}, '%') OR p.sku LIKE CONCAT('%', #{keyword}, '%') OR #{keyword} IS NULL) " +
            "GROUP BY i.product_id")
    IPage<InventorySummaryDTO> selectInventorySummary(IPage<InventorySummaryDTO> page, @Param("keyword") String keyword);
}