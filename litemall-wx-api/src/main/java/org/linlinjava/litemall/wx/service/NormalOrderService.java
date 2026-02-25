package org.linlinjava.litemall.wx.service;

import org.linlinjava.litemall.db.domain.*;
import org.linlinjava.litemall.db.service.*;
import org.linlinjava.litemall.db.util.OrderUtil;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NormalOrderService {
    private final Log logger = LogFactory.getLog(NormalOrderService.class);

    @Autowired
    private LitemallOrderService orderService;

    @Autowired
    private LitemallGoodsProductService goodsProductService;

    @Autowired
    private LitemallOrderGoodsService orderGoodsService;

    public boolean isNormalOrder(LitemallOrder order) {
        if (order == null) {
            return false;
        }
        Byte orderType = order.getOrderType();
        return orderType == null || orderType == LitemallOrder.ORDER_TYPE_NORMAL;
    }

    @Transactional
    public void setupNormalOrder(LitemallOrder order, BigDecimal actualPrice, BigDecimal freightPrice) {
        order.setOrderType(LitemallOrder.ORDER_TYPE_NORMAL);
        order.setPointsUsed(0);
        order.setActualPrice(actualPrice);
        order.setFreightPrice(freightPrice);
        order.setOrderStatus(OrderUtil.STATUS_CREATE);
        order.setVoucherStatus((short) 0);
        logger.info("普通订单设置完成：orderId=" + order.getId() + ", actualPrice=" + actualPrice);
    }

    @Transactional
    public boolean cancelNormalOrder(LitemallOrder order) {
        if (!isNormalOrder(order)) {
            return true;
        }

        order.setOrderStatus(OrderUtil.STATUS_CANCEL);
        order.setEndTime(LocalDateTime.now());
        orderService.updateSelective(order);
        
        logger.info("普通订单取消成功：orderId=" + order.getId());
        return true;
    }

    @Transactional
    public boolean approveNormalOrder(LitemallOrder order) {
        if (!isNormalOrder(order)) {
            return true;
        }

        order.setOrderStatus(OrderUtil.STATUS_PAY);
        order.setVoucherStatus((short) 1);
        order.setPayTime(LocalDateTime.now());
        orderService.updateSelective(order);

        logger.info("普通订单审核通过：orderId=" + order.getId());
        return true;
    }

    @Transactional
    public boolean rejectNormalOrder(LitemallOrder order) {
        if (!isNormalOrder(order)) {
            return true;
        }

        order.setOrderStatus(OrderUtil.STATUS_CANCEL);
        order.setVoucherStatus((short) 2);
        order.setEndTime(LocalDateTime.now());
        orderService.updateSelective(order);
        
        logger.info("普通订单审核拒绝：orderId=" + order.getId());
        return true;
    }

    @Transactional
    public boolean shipNormalOrder(LitemallOrder order, String shipChannel, String shipSn) {
        if (!isNormalOrder(order)) {
            return true;
        }

        order.setOrderStatus(OrderUtil.STATUS_SHIP);
        order.setShipChannel(shipChannel);
        order.setShipSn(shipSn);
        order.setShipTime(LocalDateTime.now());
        orderService.updateSelective(order);

        logger.info("普通订单发货成功：orderId=" + order.getId());
        return true;
    }

    @Transactional
    public boolean confirmNormalOrder(LitemallOrder order) {
        if (!isNormalOrder(order)) {
            return true;
        }

        order.setOrderStatus(OrderUtil.STATUS_CONFIRM);
        order.setConfirmTime(LocalDateTime.now());
        orderService.updateSelective(order);

        logger.info("普通订单确认收货：orderId=" + order.getId());
        return true;
    }

    @Transactional
    public boolean completeNormalOrder(LitemallOrder order) {
        if (!isNormalOrder(order)) {
            return true;
        }

        order.setOrderStatus(OrderUtil.STATUS_CONFIRM);
        order.setEndTime(LocalDateTime.now());
        orderService.updateSelective(order);

        logger.info("普通订单完成：orderId=" + order.getId());
        return true;
    }

    @Transactional
    public void restoreStock(List<LitemallCart> cartList) {
        for (LitemallCart cart : cartList) {
            Integer productId = cart.getProductId();
            LitemallGoodsProduct product = goodsProductService.findById(productId);
            if (product != null) {
                int newNumber = product.getNumber() + cart.getNumber();
                product.setNumber(newNumber);
                goodsProductService.updateById(product);
                logger.info("库存恢复：productId=" + productId + ", restored=" + cart.getNumber() + ", newNumber=" + newNumber);
            }
        }
    }
}
