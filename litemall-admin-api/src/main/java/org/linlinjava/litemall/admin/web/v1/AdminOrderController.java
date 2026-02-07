package org.linlinjava.litemall.admin.web.v1;

import org.linlinjava.litemall.admin.util.ResponseV1Util;
import org.linlinjava.litemall.admin.service.AdminOrderService;
import org.linlinjava.litemall.core.util.JacksonUtil;
import org.linlinjava.litemall.db.domain.LitemallOrder;
import org.linlinjava.litemall.db.service.LitemallOrderService;
import org.linlinjava.litemall.db.util.OrderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController("adminOrderControllerV1")
@RequestMapping("/api/v1/admin/order")
@Validated
public class AdminOrderController {
    @Autowired
    private AdminOrderService adminOrderService;
    @Autowired
    private LitemallOrderService orderService;

    @GetMapping("/list")
    public Object list() {
        Map<String, Object> data = (Map<String, Object>) adminOrderService.list(null, null, null, null, null, null, 1, 20, "add_time", "desc");
        return ResponseV1Util.ok(data);
    }

    @PostMapping("/{orderId}/ship")
    public Object ship(@PathVariable Integer orderId, @RequestBody Map<String, String> body) {
        String logisticsCompany = body.get("logisticsCompany");
        String trackingNo = body.get("trackingNo");
        Map<String, Object> req = new HashMap<>();
        req.put("orderId", orderId);
        req.put("shipSn", trackingNo);
        req.put("shipChannel", logisticsCompany);
        Object res = adminOrderService.ship(JacksonUtil.toJson(req));
        return ResponseV1Util.ok(res);
    }

    @PostMapping("/{orderId}/audit")
    public Object audit(@PathVariable Integer orderId, @RequestBody Map<String, String> body) {
        LitemallOrder order = orderService.findById(orderId);
        if (order == null) {
            return ResponseV1Util.fail(2001, "参数错误");
        }
        order.setOrderStatus(OrderUtil.STATUS_PAY);
        orderService.updateWithOptimisticLocker(order);
        return ResponseV1Util.ok();
    }

    @DeleteMapping("/{orderNo}")
    public Object delete(@PathVariable String orderNo) {
        LitemallOrder order = orderService.findBySn(orderNo);
        if (order == null) {
            return ResponseV1Util.fail(2001, "参数错误");
        }
        Map<String, Object> req = new HashMap<>();
        req.put("orderId", order.getId());
        adminOrderService.delete(JacksonUtil.toJson(req));
        return ResponseV1Util.ok();
    }
}
