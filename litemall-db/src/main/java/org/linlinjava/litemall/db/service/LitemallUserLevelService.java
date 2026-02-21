package org.linlinjava.litemall.db.service;

import org.linlinjava.litemall.db.dao.LitemallUserLevelMapper;
import org.linlinjava.litemall.db.domain.LitemallUserLevel;
import org.linlinjava.litemall.db.domain.LitemallUserLevelExample;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LitemallUserLevelService {
    @Resource
    private LitemallUserLevelMapper userLevelMapper;

    public List<LitemallUserLevel> findAll() {
        LitemallUserLevelExample example = new LitemallUserLevelExample();
        example.setOrderByClause("sort_order ASC, id ASC");
        return userLevelMapper.selectByExample(example);
    }

    public LitemallUserLevel findById(Integer id) {
        return userLevelMapper.selectByPrimaryKey(id);
    }

    public LitemallUserLevel findByExperience(Integer experience) {
        List<LitemallUserLevel> levels = findAll();
        LitemallUserLevel result = null;
        for (LitemallUserLevel level : levels) {
            if (experience >= level.getMinExperience()) {
                result = level;
            }
        }
        return result;
    }

    public int add(LitemallUserLevel level) {
        level.setAddTime(LocalDateTime.now());
        level.setUpdateTime(LocalDateTime.now());
        level.setDeleted(false);
        return userLevelMapper.insertSelective(level);
    }

    public int update(LitemallUserLevel level) {
        level.setUpdateTime(LocalDateTime.now());
        return userLevelMapper.updateByPrimaryKeySelective(level);
    }

    public int delete(Integer id) {
        LitemallUserLevel level = new LitemallUserLevel();
        level.setId(id);
        level.setDeleted(true);
        level.setUpdateTime(LocalDateTime.now());
        return userLevelMapper.updateByPrimaryKeySelective(level);
    }
}
