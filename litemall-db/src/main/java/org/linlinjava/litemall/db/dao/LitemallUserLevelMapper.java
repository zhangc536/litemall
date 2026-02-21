package org.linlinjava.litemall.db.dao;

import org.linlinjava.litemall.db.domain.LitemallUserLevel;
import org.linlinjava.litemall.db.domain.LitemallUserLevelExample;
import java.util.List;

public interface LitemallUserLevelMapper {
    long countByExample(LitemallUserLevelExample example);

    int deleteByExample(LitemallUserLevelExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(LitemallUserLevel record);

    int insertSelective(LitemallUserLevel record);

    List<LitemallUserLevel> selectByExample(LitemallUserLevelExample example);

    LitemallUserLevel selectByPrimaryKey(Integer id);

    int updateByExampleSelective(LitemallUserLevel record, LitemallUserLevelExample example);

    int updateByExample(LitemallUserLevel record, LitemallUserLevelExample example);

    int updateByPrimaryKeySelective(LitemallUserLevel record);

    int updateByPrimaryKey(LitemallUserLevel record);
}
