package org.linlinjava.litemall.wx.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.db.domain.LitemallGoods;
import org.linlinjava.litemall.db.domain.LitemallPointGoods;
import org.linlinjava.litemall.db.service.LitemallGoodsService;
import org.linlinjava.litemall.db.service.LitemallPointGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 积分商品服务
 */
@RestController
@RequestMapping("/wx/pointGoods")
@Validated
public class WxPointGoodsController {
    private final Log logger = LogFactory.getLog(WxPointGoodsController.class);

    @Autowired
    private LitemallPointGoodsService pointGoodsService;

    @Autowired
    private LitemallGoodsService goodsService;

    /**
     * 积分商品列表
     *
     * @param page 分页页数
     * @param limit 分页大小
     * @return 积分商品列表
     */
    @GetMapping("list")
    public Object list(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit) {
        List<LitemallPointGoods> pointGoodsList = pointGoodsService.querySelective(null, page, limit, "add_time", "desc");
        List<LitemallPointGoods> onSaleList = new ArrayList<>();
        for (LitemallPointGoods item : pointGoodsList) {
            LitemallGoods goods = goodsService.findByIdVO(item.getGoodsId());
            if (goods != null) {
                onSaleList.add(item);
            }
        }

        long total = onSaleList.size();
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("items", onSaleList);

        return ResponseUtil.ok(data);
    }

    @GetMapping("detail")
    public Object detail(@NotNull Integer goodsId) {
        LitemallGoods goods = goodsService.findByIdVO(goodsId);
        if (goods == null) {
            return ResponseUtil.badArgumentValue();
        }
        LitemallPointGoods pointGoods = pointGoodsService.findByGoodsId(goodsId);
        if (pointGoods == null) {
            return ResponseUtil.badArgumentValue();
        }
        return ResponseUtil.ok(pointGoods);
    }
}
