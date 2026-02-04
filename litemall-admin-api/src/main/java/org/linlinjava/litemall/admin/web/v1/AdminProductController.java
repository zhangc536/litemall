package org.linlinjava.litemall.admin.web.v1;

import org.linlinjava.litemall.admin.util.ResponseV1Util;
import org.linlinjava.litemall.db.domain.LitemallGoods;
import org.linlinjava.litemall.db.service.LitemallGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/product")
@Validated
public class AdminProductController {
    @Autowired
    private LitemallGoodsService goodsService;

    @GetMapping
    public Object list() {
        List<LitemallGoods> goodsList = goodsService.querySelective(null, null, null, 1, 100, "add_time", "desc");
        return ResponseV1Util.ok(goodsList);
    }

    @PostMapping
    public Object create(@RequestBody LitemallGoods goods) {
        goodsService.add(goods);
        return ResponseV1Util.ok(goods);
    }

    @PutMapping("/{id}")
    public Object update(@PathVariable Integer id, @RequestBody LitemallGoods goods) {
        goods.setId(id);
        goodsService.updateById(goods);
        return ResponseV1Util.ok(goods);
    }

    @DeleteMapping("/{id}")
    public Object delete(@PathVariable Integer id) {
        goodsService.deleteById(id);
        return ResponseV1Util.ok();
    }
}
