package org.linlinjava.litemall.db.service;

import org.linlinjava.litemall.db.dao.LitemallPointsLogMapper;
import org.linlinjava.litemall.db.domain.LitemallPointsLog;
import org.linlinjava.litemall.db.domain.LitemallUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LitemallPointsLogService {
    private static final Logger logger = LoggerFactory.getLogger(LitemallPointsLogService.class);

    @Resource
    private LitemallPointsLogMapper pointsLogMapper;

    @Resource
    private LitemallUserService userService;

    public List<LitemallPointsLog> queryByUserId(Integer userId) {
        return pointsLogMapper.selectByUserId(userId);
    }

    public LitemallPointsLog queryByOrderId(Integer orderId) {
        return pointsLogMapper.selectByOrderId(orderId);
    }

    @Transactional
    public boolean deductPoints(Integer userId, Integer points, Integer orderId, String orderSn, String description) {
        if (points == null || points <= 0) {
            logger.warn("积分扣除失败：积分数值无效, userId={}, points={}", userId, points);
            return false;
        }

        LitemallUser user = userService.findById(userId);
        if (user == null) {
            logger.error("积分扣除失败：用户不存在, userId={}", userId);
            return false;
        }

        int currentPoints = user.getPoints() == null ? 0 : user.getPoints();
        if (currentPoints < points) {
            logger.warn("积分扣除失败：积分不足, userId={}, currentPoints={}, requiredPoints={}", userId, currentPoints, points);
            return false;
        }

        int newPoints = currentPoints - points;
        LitemallUser updateUser = new LitemallUser();
        updateUser.setId(userId);
        updateUser.setPoints(newPoints);
        userService.updateById(updateUser);

        LitemallPointsLog log = new LitemallPointsLog();
        log.setUserId(userId);
        log.setPoints(-points);
        log.setType(LitemallPointsLog.TYPE_POINT_EXCHANGE);
        log.setOrderId(orderId);
        log.setOrderSn(orderSn);
        log.setDescription(description != null ? description : "积分兑换商品");
        log.setBalanceAfter(newPoints);
        log.setAddTime(LocalDateTime.now());
        pointsLogMapper.insertSelective(log);

        logger.info("积分扣除成功：userId={}, points={}, newBalance={}, orderId={}", userId, points, newPoints, orderId);
        return true;
    }

    @Transactional
    public boolean refundPoints(Integer userId, Integer points, Integer orderId, String orderSn, byte type, String description) {
        if (points == null || points <= 0) {
            logger.warn("积分返还失败：积分数值无效, userId={}, points={}", userId, points);
            return false;
        }

        LitemallUser user = userService.findById(userId);
        if (user == null) {
            logger.error("积分返还失败：用户不存在, userId={}", userId);
            return false;
        }

        LitemallPointsLog existLog = pointsLogMapper.selectByOrderId(orderId);
        if (existLog != null && existLog.getType() == type) {
            logger.warn("积分返还失败：已存在相同类型的返还记录, orderId={}, type={}", orderId, type);
            return false;
        }

        int currentPoints = user.getPoints() == null ? 0 : user.getPoints();
        int newPoints = currentPoints + points;

        LitemallUser updateUser = new LitemallUser();
        updateUser.setId(userId);
        updateUser.setPoints(newPoints);
        userService.updateById(updateUser);

        LitemallPointsLog log = new LitemallPointsLog();
        log.setUserId(userId);
        log.setPoints(points);
        log.setType(type);
        log.setOrderId(orderId);
        log.setOrderSn(orderSn);
        log.setDescription(description);
        log.setBalanceAfter(newPoints);
        log.setAddTime(LocalDateTime.now());
        pointsLogMapper.insertSelective(log);

        logger.info("积分返还成功：userId={}, points={}, newBalance={}, orderId={}, type={}", userId, points, newPoints, orderId, type);
        return true;
    }

    @Transactional
    public boolean addPoints(Integer userId, Integer points, Integer orderId, String orderSn, String description) {
        if (points == null || points <= 0) {
            return false;
        }

        LitemallUser user = userService.findById(userId);
        if (user == null) {
            return false;
        }

        int currentPoints = user.getPoints() == null ? 0 : user.getPoints();
        int newPoints = currentPoints + points;

        LitemallUser updateUser = new LitemallUser();
        updateUser.setId(userId);
        updateUser.setPoints(newPoints);
        userService.updateById(updateUser);

        LitemallPointsLog log = new LitemallPointsLog();
        log.setUserId(userId);
        log.setPoints(points);
        log.setType(LitemallPointsLog.TYPE_ORDER_REWARD);
        log.setOrderId(orderId);
        log.setOrderSn(orderSn);
        log.setDescription(description != null ? description : "订单完成奖励积分");
        log.setBalanceAfter(newPoints);
        log.setAddTime(LocalDateTime.now());
        pointsLogMapper.insertSelective(log);

        logger.info("积分奖励成功：userId={}, points={}, newBalance={}", userId, points, newPoints);
        return true;
    }
}
