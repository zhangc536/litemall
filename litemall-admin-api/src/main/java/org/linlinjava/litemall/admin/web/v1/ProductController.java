package org.linlinjava.litemall.admin.web.v1;

import org.linlinjava.litemall.admin.util.ResponseV1Util;
import org.linlinjava.litemall.db.domain.LitemallGoods;
import org.linlinjava.litemall.db.service.LitemallGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product")
@Validated
public class ProductController {
    @Autowired
    private LitemallGoodsService goodsService;

    @GetMapping("/list")
    public Object list() {
        List<LitemallGoods> goodsList = goodsService.querySelective(null, null, null, 1, 100, "add_time", "desc");
        return ResponseV1Util.ok(goodsList);
    }

    @GetMapping("/{id}")
    public Object read(@PathVariable Integer id) {
        LitemallGoods goods = goodsService.findById(id);
        return ResponseV1Util.ok(goods);
    }
}
