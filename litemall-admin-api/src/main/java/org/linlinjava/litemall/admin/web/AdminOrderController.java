package org.linlinjava.litemall.admin.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.linlinjava.litemall.admin.annotation.RequiresPermissionsDesc;
import org.linlinjava.litemall.admin.service.AdminOrderService;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.db.domain.LitemallOrder;
import org.linlinjava.litemall.db.service.LitemallOrderService;
import org.linlinjava.litemall.db.util.OrderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/order")
@Validated
public class AdminOrderController {
    private final Log logger = LogFactory.getLog(AdminOrderController.class);

    @Autowired
    private LitemallOrderService orderService;
    @Autowired
    private AdminOrderService adminOrderService;

    @RequiresPermissions("admin:order:audit")
    @RequiresPermissionsDesc(menu = {"商品管理", "凭证审核"}, button = "审核")
    @PostMapping("/audit")
    public Object audit(@RequestBody Map<String, String> body) {
        try {
            Integer orderId = Integer.valueOf(body.getOrDefault("orderId", "0"));
            String status = body.get("status"); // APPROVED | REJECTED
            String remark = body.get("remark"); // 可选
            if (orderId == null || orderId <= 0 || status == null) {
                return ResponseUtil.badArgument();
            }
            LitemallOrder order = orderService.findById(orderId);
            if (order == null) {
                return ResponseUtil.badArgument();
            }
            if ("APPROVED".equalsIgnoreCase(status)) {
                order.setOrderStatus(OrderUtil.STATUS_PAY);
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                order.setOrderStatus(OrderUtil.STATUS_CANCEL);
            } else {
                return ResponseUtil.badArgument();
            }
            orderService.updateWithOptimisticLocker(order);
            return ResponseUtil.ok();
        } catch (Exception e) {
            logger.error("Order audit error", e);
            return ResponseUtil.serious();
        }
    }

    @RequiresPermissions("admin:order:ship")
    @RequiresPermissionsDesc(menu = {"商品管理", "凭证审核"}, button = "发货")
    @PostMapping("/ship")
    public Object ship(@RequestBody Map<String, String> body) {
        try {
            Integer orderId = Integer.valueOf(body.getOrDefault("orderId", "0"));
            String shipChannel = body.get("logisticsCompany");
            String shipSn = body.get("trackingNo");
            if (orderId == null || orderId <= 0 || shipChannel == null || shipSn == null) {
                return ResponseUtil.badArgument();
            }
            Map<String, Object> req = new HashMap<>();
            req.put("orderId", orderId);
            req.put("shipSn", shipSn);
            req.put("shipChannel", shipChannel);
            // 复用已有服务逻辑
            return adminOrderService.ship(org.linlinjava.litemall.core.util.JacksonUtil.toJson(req));
        } catch (Exception e) {
            logger.error("Order ship error", e);
            return ResponseUtil.serious();
        }
    }
}
