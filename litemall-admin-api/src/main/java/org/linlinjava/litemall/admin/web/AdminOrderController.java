package org.linlinjava.litemall.admin.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.linlinjava.litemall.admin.annotation.RequiresPermissionsDesc;
import org.linlinjava.litemall.admin.service.AdminOrderService;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.db.domain.LitemallOrder;
import org.linlinjava.litemall.db.domain.LitemallStorage;
import org.linlinjava.litemall.db.service.LitemallOrderService;
import org.linlinjava.litemall.db.service.LitemallStorageService;
import org.linlinjava.litemall.db.util.OrderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
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
    @Autowired
    private LitemallStorageService storageService;

    @RequiresPermissions("admin:order:list")
    @RequiresPermissionsDesc(menu = {"订单管理", "订单列表"}, button = "查询")
    @GetMapping("/list")
    public Object list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            String orderSn,
            Short orderStatus,
            String startTime,
            String endTime) {
        List<LitemallOrder> orderList = orderService.queryOrderList(orderSn, orderStatus, startTime, endTime, page, limit);
        int total = orderService.countOrderList(orderSn, orderStatus, startTime, endTime);
        return ResponseUtil.okList(orderList, total);
    }

    @RequiresPermissions("admin:order:list")
    @RequiresPermissionsDesc(menu = {"订单管理", "订单列表"}, button = "详情")
    @GetMapping("/detail")
    public Object detail(@RequestParam Integer id) {
        if (id == null || id <= 0) {
            return ResponseUtil.badArgument();
        }
        LitemallOrder order = orderService.findById(id);
        if (order == null) {
            return ResponseUtil.badArgument();
        }
        return ResponseUtil.ok(order);
    }

    @RequiresPermissions("admin:order:list")
    @RequiresPermissionsDesc(menu = {"商品管理", "凭证审核"}, button = "查询")
    @GetMapping("/voucher/list")
    public Object voucherList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            String orderSn,
            Short voucherStatus) {
        List<LitemallOrder> orderList = orderService.queryVoucherList(orderSn, voucherStatus, page, limit);
        if (orderList.isEmpty()) {
            boolean updated = syncVoucherFromStorage(orderSn);
            if (updated) {
                orderList = orderService.queryVoucherList(orderSn, voucherStatus, page, limit);
            }
        }
        return ResponseUtil.okList(orderList);
    }

    @RequiresPermissions("admin:order:audit")
    @RequiresPermissionsDesc(menu = {"商品管理", "凭证审核"}, button = "审核")
    @PostMapping("/audit")
    public Object audit(@RequestBody Map<String, String> body) {
        try {
            Integer orderId = Integer.valueOf(body.getOrDefault("orderId", "0"));
            String status = body.get("status");
            String remark = body.get("remark");
            if (orderId == null || orderId <= 0 || status == null) {
                return ResponseUtil.badArgument();
            }
            LitemallOrder order = orderService.findById(orderId);
            if (order == null) {
                return ResponseUtil.badArgument();
            }
            if ("APPROVED".equalsIgnoreCase(status)) {
                order.setOrderStatus(OrderUtil.STATUS_PAY);
                order.setVoucherStatus((short) 1);
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                order.setVoucherStatus((short) 2);
            } else {
                return ResponseUtil.badArgument();
            }
            order.setUpdateTime(LocalDateTime.now());
            if (orderService.updateSelective(order) == 0) {
                return ResponseUtil.updatedDataFailed();
            }
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
            return adminOrderService.ship(org.linlinjava.litemall.core.util.JacksonUtil.toJson(req));
        } catch (Exception e) {
            logger.error("Order ship error", e);
            return ResponseUtil.serious();
        }
    }

    private boolean syncVoucherFromStorage(String orderSn) {
        List<LitemallStorage> storages = storageService.querySelective(null, "voucher_", 1, 1000, "add_time", "desc");
        if (storages == null || storages.isEmpty()) {
            return false;
        }
        boolean updated = false;
        for (LitemallStorage storage : storages) {
            Integer orderId = parseOrderIdFromVoucherName(storage.getName());
            if (orderId == null) {
                continue;
            }
            LitemallOrder order = orderService.findById(orderId);
            if (order == null) {
                continue;
            }
            if (orderSn != null && orderSn.length() > 0 && !order.getOrderSn().contains(orderSn)) {
                continue;
            }
            boolean needUpdate = false;
            if (order.getPayVoucher() == null || order.getPayVoucher().length() == 0) {
                order.setPayVoucher(storage.getUrl());
                needUpdate = true;
            }
            if (order.getVoucherStatus() == null) {
                order.setVoucherStatus((short) 0);
                needUpdate = true;
            }
            if (needUpdate) {
                orderService.updateSelective(order);
                updated = true;
            }
        }
        return updated;
    }

    private Integer parseOrderIdFromVoucherName(String name) {
        if (name == null) {
            return null;
        }
        String prefix = "voucher_";
        int start = name.indexOf(prefix);
        if (start != 0) {
            return null;
        }
        start += prefix.length();
        int end = name.indexOf("_", start);
        if (end <= start) {
            return null;
        }
        try {
            return Integer.valueOf(name.substring(start, end));
        } catch (Exception e) {
            return null;
        }
    }
}
