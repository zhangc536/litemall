package org.linlinjava.litemall.db;

import org.junit.Test;
import org.linlinjava.litemall.db.domain.LitemallOrder;
import org.linlinjava.litemall.db.util.OrderUtil;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class OrderUtilPointOrderTest {
    @Test
    public void testPointOrderAuditText() {
        LitemallOrder order = new LitemallOrder();
        order.setOrderStatus(OrderUtil.STATUS_CREATE);
        order.setPayVoucher("积分兑换：100积分");
        order.setVoucherStatus((short) 0);
        order.setIntegralPrice(new BigDecimal("100"));
        order.setActualPrice(new BigDecimal("0"));
        assertEquals("待审核", OrderUtil.orderStatusText(order));

        order.setVoucherStatus((short) 2);
        assertEquals("已拒绝", OrderUtil.orderStatusText(order));

        order.setVoucherStatus((short) 1);
        order.setOrderStatus(OrderUtil.STATUS_PAY);
        assertEquals("已付款", OrderUtil.orderStatusText(order));
    }
}
