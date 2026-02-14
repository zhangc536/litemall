package org.linlinjava.litemall.db.service;

import com.github.pagehelper.PageHelper;
import org.linlinjava.litemall.db.dao.LitemallPointGoodsMapper;
import org.linlinjava.litemall.db.domain.LitemallPointGoods;
import org.linlinjava.litemall.db.domain.LitemallPointGoodsExample;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LitemallPointGoodsService {
    @Resource
    private LitemallPointGoodsMapper pointGoodsMapper;

    public List<LitemallPointGoods> querySelective(String goodsName, Integer page, Integer limit, String sort, String order) {
        LitemallPointGoodsExample example = new LitemallPointGoodsExample();
        LitemallPointGoodsExample.Criteria criteria = example.createCriteria();

        if (!StringUtils.isEmpty(goodsName)) {
            criteria.andGoodsNameLike("%" + goodsName + "%");
        }
        criteria.andDeletedEqualTo(false);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            if ("add_time".equalsIgnoreCase(sort)) {
                example.setOrderByClause(sort + " " + order + ", id " + order);
            } else {
                example.setOrderByClause(sort + " " + order);
            }
        }

        PageHelper.startPage(page, limit);
        return pointGoodsMapper.selectByExample(example);
    }

    public int updateById(LitemallPointGoods pointGoods) {
        pointGoods.setUpdateTime(LocalDateTime.now());
        return pointGoodsMapper.updateByPrimaryKeySelective(pointGoods);
    }

    public void deleteById(Integer id) {
        pointGoodsMapper.logicalDeleteByPrimaryKey(id);
    }

    public void add(LitemallPointGoods pointGoods) {
        pointGoods.setAddTime(LocalDateTime.now());
        pointGoods.setUpdateTime(LocalDateTime.now());
        pointGoodsMapper.insertSelective(pointGoods);
    }

    public LitemallPointGoods findById(Integer id) {
        return pointGoodsMapper.selectByPrimaryKey(id);
    }

    public LitemallPointGoods findByGoodsId(Integer goodsId) {
        LitemallPointGoodsExample example = new LitemallPointGoodsExample();
        example.or().andGoodsIdEqualTo(goodsId).andDeletedEqualTo(false);
        example.setOrderByClause("add_time desc");
        List<LitemallPointGoods> list = pointGoodsMapper.selectByExample(example);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
}
