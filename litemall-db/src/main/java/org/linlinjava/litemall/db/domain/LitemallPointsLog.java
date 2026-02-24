package org.linlinjava.litemall.db.domain;

import java.time.LocalDateTime;

public class LitemallPointsLog {
    private Integer id;
    private Integer userId;
    private Integer points;
    private Byte type;
    private Integer orderId;
    private String orderSn;
    private String description;
    private Integer balanceAfter;
    private LocalDateTime addTime;

    public static final byte TYPE_ORDER_REWARD = 1;
    public static final byte TYPE_POINT_EXCHANGE = 2;
    public static final byte TYPE_ADMIN_ADJUST = 3;
    public static final byte TYPE_ORDER_CANCEL_REFUND = 4;
    public static final byte TYPE_AUDIT_REJECT_REFUND = 5;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getOrderSn() {
        return orderSn;
    }

    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(Integer balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public LocalDateTime getAddTime() {
        return addTime;
    }

    public void setAddTime(LocalDateTime addTime) {
        this.addTime = addTime;
    }
}
