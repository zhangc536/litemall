package org.linlinjava.litemall.admin.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.linlinjava.litemall.admin.annotation.RequiresPermissionsDesc;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.core.validator.Order;
import org.linlinjava.litemall.core.validator.Sort;
import org.linlinjava.litemall.db.domain.LitemallGoods;
import org.linlinjava.litemall.db.domain.LitemallPointGoods;
import org.linlinjava.litemall.db.service.LitemallGoodsService;
import org.linlinjava.litemall.db.service.LitemallPointGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/pointGoods")
@Validated
public class AdminPointGoodsController {
    private final Log logger = LogFactory.getLog(AdminPointGoodsController.class);

    @Autowired
    private LitemallPointGoodsService pointGoodsService;

    @Autowired
    private LitemallGoodsService goodsService;

    @RequiresPermissions("admin:pointGoods:list")
    @RequiresPermissionsDesc(menu = {"积分管理", "积分商品"}, button = "查询")
    @GetMapping("/list")
    public Object list(String goodsName,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {
        List<LitemallPointGoods> pointGoodsList = pointGoodsService.querySelective(goodsName, page, limit, sort, order);
        return ResponseUtil.okList(pointGoodsList);
    }

    @RequiresPermissions("admin:pointGoods:create")
    @RequiresPermissionsDesc(menu = {"积分管理", "积分商品"}, button = "添加")
    @PostMapping("/create")
    public Object create(@RequestBody LitemallPointGoods pointGoods) {
        Object error = validate(pointGoods);
        if (error != null) {
            return error;
        }
        pointGoodsService.add(pointGoods);
        return ResponseUtil.ok(pointGoods);
    }

    @RequiresPermissions("admin:pointGoods:update")
    @RequiresPermissionsDesc(menu = {"积分管理", "积分商品"}, button = "编辑")
    @PostMapping("/update")
    public Object update(@RequestBody LitemallPointGoods pointGoods) {
        Object error = validate(pointGoods);
        if (error != null) {
            return error;
        }
        if (pointGoodsService.updateById(pointGoods) == 0) {
            return ResponseUtil.updatedDataFailed();
        }
        return ResponseUtil.ok(pointGoods);
    }

    @RequiresPermissions("admin:pointGoods:delete")
    @RequiresPermissionsDesc(menu = {"积分管理", "积分商品"}, button = "删除")
    @PostMapping("/delete")
    public Object delete(@RequestBody LitemallPointGoods pointGoods) {
        pointGoodsService.deleteById(pointGoods.getId());
        return ResponseUtil.ok();
    }

    private Object validate(LitemallPointGoods pointGoods) {
        Integer goodsId = pointGoods.getGoodsId();
        if (goodsId == null) {
            return ResponseUtil.badArgument();
        }
        LitemallGoods goods = goodsService.findByIdVO(goodsId);
        if (goods == null) {
            return ResponseUtil.fail(402, "商品已下架或不存在");
        }
        if (pointGoods.getGoodsName() == null) {
            pointGoods.setGoodsName(goods.getName());
        }
        if (pointGoods.getGoodsBrief() == null) {
            pointGoods.setGoodsBrief(goods.getBrief());
        }
        if (pointGoods.getPicUrl() == null) {
            pointGoods.setPicUrl(goods.getPicUrl());
        }
        return null;
    }
}
