package org.linlinjava.litemall.wx.service;

import org.linlinjava.litemall.db.domain.*;
import org.linlinjava.litemall.db.service.*;
import org.linlinjava.litemall.db.util.OrderUtil;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.core.system.SystemConfig;
import org.linlinjava.litemall.wx.util.WxResponseCode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class OrderRouterService {
    private final Log logger = LogFactory.getLog(OrderRouterService.class);

    @Autowired
    private NormalOrderService normalOrderService;

    @Autowired
    private PointsOrderService pointsOrderService;

    @Autowired
    private LitemallOrderService orderService;

    @Autowired
    private LitemallUserService userService;

    @Autowired
    private LitemallPointGoodsService pointGoodsService;

    @Autowired
    private LitemallPointsLogService pointsLogService;

    public boolean isPointsOrder(LitemallOrder order) {
        return order != null && order.getOrderType() != null 
                && order.getOrderType() == LitemallOrder.ORDER_TYPE_POINTS;
    }

    public boolean isNormalOrder(LitemallOrder order) {
        if (order == null) {
            return false;
        }
        Byte orderType = order.getOrderType();
        return orderType == null || orderType == LitemallOrder.ORDER_TYPE_NORMAL;
    }

    @Transactional
    public Object routeSubmit(Integer userId, Boolean usePoints, List<LitemallCart> checkedGoodsList) {
        logger.info("OrderRouter - routeSubmit: userId=" + userId + ", usePoints=" + usePoints);
        
        boolean requestPointsOrder = usePoints != null && usePoints;
        
        if (requestPointsOrder) {
            return processPointsOrderSubmit(userId, checkedGoodsList);
        } else {
            return processNormalOrderSubmit(userId, checkedGoodsList);
        }
    }

    private Object processPointsOrderSubmit(Integer userId, List<LitemallCart> checkedGoodsList) {
        logger.info("Processing as Points Order");
        
        int requiredPoints = pointsOrderService.calculateRequiredPoints(checkedGoodsList);
        if (requiredPoints < 0) {
            logger.error("积分订单计算失败：购物车中包含非积分商品");
            return ResponseUtil.fail(WxResponseCode.ORDER_CHECKOUT_FAIL, "购物车中包含非积分商品，无法使用积分支付");
        }
        
        if (requiredPoints == 0) {
            logger.error("积分订单计算失败：购物车中没有有效的积分商品");
            return ResponseUtil.fail(WxResponseCode.ORDER_CHECKOUT_FAIL, "积分商品信息异常，请确认商品是否为积分商品");
        }
        
        if (!pointsOrderService.validatePointsBalance(userId, requiredPoints)) {
            LitemallUser user = userService.findById(userId);
            int currentPoints = (user != null && user.getPoints() != null) ? user.getPoints() : 0;
            logger.error("积分不足：userId=" + userId + ", currentPoints=" + currentPoints + ", requiredPoints=" + requiredPoints);
            return ResponseUtil.fail(WxResponseCode.ORDER_CHECKOUT_FAIL, "积分不足，当前积分：" + currentPoints + "，需要积分：" + requiredPoints);
        }
        
        logger.info("积分订单验证通过：requiredPoints=" + requiredPoints);
        return null;
    }

    private Object processNormalOrderSubmit(Integer userId, List<LitemallCart> checkedGoodsList) {
        logger.info("Processing as Normal Order - No points validation required");
        return null;
    }

    @Transactional
    public void setupOrder(LitemallOrder order, boolean isPointsOrder, int requiredPoints, 
                          BigDecimal actualPrice, BigDecimal freightPrice) {
        if (isPointsOrder) {
            pointsOrderService.setupPointsOrder(order, requiredPoints);
        } else {
            normalOrderService.setupNormalOrder(order, actualPrice, freightPrice);
        }
    }

    @Transactional
    public boolean processPostOrderCreation(Integer userId, boolean isPointsOrder, 
                                           int requiredPoints, Integer orderId, String orderSn) {
        if (isPointsOrder && requiredPoints > 0) {
            boolean success = pointsOrderService.deductPointsForOrder(userId, requiredPoints, orderId, orderSn);
            if (!success) {
                throw new RuntimeException("积分扣除失败，请重试");
            }
            logger.info("积分订单积分扣除成功：userId=" + userId + ", points=" + requiredPoints + ", orderId=" + orderId);
            return success;
        }
        return true;
    }

    @Transactional
    public boolean cancelOrder(LitemallOrder order) {
        if (isPointsOrder(order)) {
            return pointsOrderService.cancelPointsOrder(order);
        } else {
            return normalOrderService.cancelNormalOrder(order);
        }
    }

    @Transactional
    public boolean approveOrder(LitemallOrder order) {
        if (isPointsOrder(order)) {
            return pointsOrderService.approvePointsOrder(order);
        } else {
            return normalOrderService.approveNormalOrder(order);
        }
    }

    @Transactional
    public boolean rejectOrder(LitemallOrder order) {
        if (isPointsOrder(order)) {
            return pointsOrderService.rejectPointsOrder(order);
        } else {
            return normalOrderService.rejectNormalOrder(order);
        }
    }

    @Transactional
    public boolean shipOrder(LitemallOrder order, String shipChannel, String shipSn) {
        if (isPointsOrder(order)) {
            return pointsOrderService.shipPointsOrder(order, shipChannel, shipSn);
        } else {
            return normalOrderService.shipNormalOrder(order, shipChannel, shipSn);
        }
    }

    @Transactional
    public boolean confirmOrder(LitemallOrder order) {
        if (isPointsOrder(order)) {
            return pointsOrderService.confirmPointsOrder(order);
        } else {
            return normalOrderService.confirmNormalOrder(order);
        }
    }

    @Transactional
    public boolean completeOrder(LitemallOrder order) {
        if (isPointsOrder(order)) {
            return pointsOrderService.completePointsOrder(order);
        } else {
            return normalOrderService.completeNormalOrder(order);
        }
    }

    @Transactional
    public void restoreStock(List<LitemallCart> cartList) {
        normalOrderService.restoreStock(cartList);
    }
}
