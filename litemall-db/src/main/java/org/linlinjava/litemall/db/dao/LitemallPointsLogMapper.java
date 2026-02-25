package org.linlinjava.litemall.db.dao;

import org.apache.ibatis.annotations.Param;
import org.linlinjava.litemall.db.domain.LitemallPointsLog;
import java.util.List;

public interface LitemallPointsLogMapper {
    int insertSelective(LitemallPointsLog record);

    List<LitemallPointsLog> selectByUserId(Integer userId);

    LitemallPointsLog selectByOrderId(Integer orderId);

    LitemallPointsLog selectByOrderIdAndType(@Param("orderId") Integer orderId, @Param("type") Byte type);
}
