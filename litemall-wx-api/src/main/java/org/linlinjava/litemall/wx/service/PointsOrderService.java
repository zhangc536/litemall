package org.linlinjava.litemall.wx.service;

import org.linlinjava.litemall.db.domain.*;
import org.linlinjava.litemall.db.service.*;
import org.linlinjava.litemall.db.util.OrderUtil;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.db.domain.LitemallPointsLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PointsOrderService {
    private static final Logger logger = LoggerFactory.getLogger(PointsOrderService.class);

    @Autowired
    private LitemallOrderService orderService;

    @Autowired
    private LitemallUserService userService;

    @Autowired
    private LitemallPointsLogService pointsLogService;

    @Autowired
    private LitemallPointGoodsService pointGoodsService;

    @Autowired
    private LitemallGoodsProductService goodsProductService;

    @Autowired
    private LitemallOrderGoodsService orderGoodsService;

    public boolean isPointsOrder(LitemallOrder order) {
        return order != null && order.getOrderType() != null 
                && order.getOrderType() == LitemallOrder.ORDER_TYPE_POINTS;
    }

    public int calculateRequiredPoints(List<LitemallCart> cartList) {
        int totalPoints = 0;
        for (LitemallCart cart : cartList) {
            LitemallPointGoods pointGoods = pointGoodsService.findByGoodsId(cart.getGoodsId());
            if (pointGoods != null && pointGoods.getPoints() != null && pointGoods.getPoints() > 0) {
                totalPoints += pointGoods.getPoints() * cart.getNumber();
            } else {
                return -1;
            }
        }
        return totalPoints;
    }

    public boolean validatePointsBalance(Integer userId, int requiredPoints) {
        LitemallUser user = userService.findById(userId);
        if (user == null) {
            return false;
        }
        int currentPoints = user.getPoints() == null ? 0 : user.getPoints();
        return currentPoints >= requiredPoints;
    }

    @Transactional
    public void setupPointsOrder(LitemallOrder order, int requiredPoints) {
        order.setOrderType(LitemallOrder.ORDER_TYPE_POINTS);
        order.setPointsUsed(requiredPoints);
        order.setIntegralPrice(new BigDecimal(requiredPoints));
        order.setActualPrice(BigDecimal.ZERO);
        order.setPayVoucher("积分兑换：" + requiredPoints + "积分");
        order.setVoucherStatus((short) 0);
        order.setOrderStatus(OrderUtil.STATUS_CREATE);
        logger.info("积分订单设置完成：orderId={}, pointsUsed={}", order.getId(), requiredPoints);
    }

    @Transactional
    public boolean deductPointsForOrder(Integer userId, int points, Integer orderId, String orderSn) {
        String description = "积分兑换商品，订单号：" + orderSn;
        boolean success = pointsLogService.deductPoints(userId, points, orderId, orderSn, description);
        if (success) {
            logger.info("积分扣除成功：userId={}, points={}, orderId={}", userId, points, orderId);
        } else {
            logger.error("积分扣除失败：userId={}, points={}, orderId={}", userId, points, orderId);
        }
        return success;
    }

    @Transactional
    public boolean refundPointsForOrder(LitemallOrder order, byte refundType, String description) {
        if (!isPointsOrder(order)) {
            return true;
        }

        Integer pointsUsed = order.getPointsUsed();
        if (pointsUsed == null || pointsUsed <= 0) {
            return true;
        }

        boolean success = pointsLogService.refundPoints(
                order.getUserId(),
                pointsUsed,
                order.getId(),
                order.getOrderSn(),
                refundType,
                description
        );

        if (success) {
            logger.info("积分返还成功：userId={}, points={}, orderId={}, type={}",
                    order.getUserId(), pointsUsed, order.getId(), refundType);
        } else {
            logger.error("积分返还失败：userId={}, points={}, orderId={}",
                    order.getUserId(), pointsUsed, order.getId());
        }
        return success;
    }

    @Transactional
    public boolean cancelPointsOrder(LitemallOrder order) {
        if (!isPointsOrder(order)) {
            return true;
        }

        boolean refundSuccess = refundPointsForOrder(
                order,
                LitemallPointsLog.TYPE_ORDER_CANCEL_REFUND,
                "订单取消，积分返还"
        );

        if (refundSuccess) {
            order.setOrderStatus(OrderUtil.STATUS_CANCEL);
            order.setEndTime(LocalDateTime.now());
            orderService.updateSelective(order);
            logger.info("积分订单取消成功：orderId={}", order.getId());
        }

        return refundSuccess;
    }

    @Transactional
    public boolean approvePointsOrder(LitemallOrder order) {
        if (!isPointsOrder(order)) {
            return true;
        }

        order.setOrderStatus(OrderUtil.STATUS_PAY);
        order.setVoucherStatus((short) 1);
        order.setPayTime(LocalDateTime.now());
        orderService.updateSelective(order);

        logger.info("积分订单审核通过：orderId={}", order.getId());
        return true;
    }

    @Transactional
    public boolean rejectPointsOrder(LitemallOrder order) {
        if (!isPointsOrder(order)) {
            return true;
        }

        boolean refundSuccess = refundPointsForOrder(
                order,
                LitemallPointsLog.TYPE_AUDIT_REJECT_REFUND,
                "审核拒绝，积分返还"
        );

        if (refundSuccess) {
            order.setOrderStatus(OrderUtil.STATUS_CANCEL);
            order.setVoucherStatus((short) 2);
            order.setEndTime(LocalDateTime.now());
            orderService.updateSelective(order);
            logger.info("积分订单审核拒绝：orderId={}", order.getId());
        }

        return refundSuccess;
    }

    @Transactional
    public boolean shipPointsOrder(LitemallOrder order, String shipChannel, String shipSn) {
        if (!isPointsOrder(order)) {
            return true;
        }

        order.setOrderStatus(OrderUtil.STATUS_SHIP);
        order.setShipChannel(shipChannel);
        order.setShipSn(shipSn);
        order.setShipTime(LocalDateTime.now());
        orderService.updateSelective(order);

        logger.info("积分订单发货成功：orderId={}, shipChannel={}, shipSn={}", order.getId(), shipChannel, shipSn);
        return true;
    }

    @Transactional
    public boolean confirmPointsOrder(LitemallOrder order) {
        if (!isPointsOrder(order)) {
            return true;
        }

        order.setOrderStatus(OrderUtil.STATUS_CONFIRM);
        order.setConfirmTime(LocalDateTime.now());
        orderService.updateSelective(order);

        logger.info("积分订单确认收货：orderId={}", order.getId());
        return true;
    }

    @Transactional
    public boolean completePointsOrder(LitemallOrder order) {
        if (!isPointsOrder(order)) {
            return true;
        }

        order.setOrderStatus(OrderUtil.STATUS_COMPLETE);
        order.setEndTime(LocalDateTime.now());
        orderService.updateSelective(order);

        logger.info("积分订单完成：orderId={}", order.getId());
        return true;
    }
}
