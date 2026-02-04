package org.linlinjava.litemall.wx.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.core.storage.StorageService;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.db.domain.LitemallOrder;
import org.linlinjava.litemall.db.domain.LitemallStorage;
import org.linlinjava.litemall.db.service.LitemallOrderService;
import org.linlinjava.litemall.db.service.LitemallStorageService;
import org.linlinjava.litemall.wx.annotation.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/wx/order/voucher")
@Validated
public class WxVoucherController {
    private final Log logger = LogFactory.getLog(WxVoucherController.class);

    @Autowired
    private StorageService storageService;
    @Autowired
    private LitemallStorageService storageServiceDb;
    @Autowired
    private LitemallOrderService orderService;

    @GetMapping("/list")
    public Object list(@LoginUser Integer userId, @RequestParam Integer orderId) {
        if (userId == null || orderId == null || orderId <= 0) {
            return ResponseUtil.unlogin();
        }
        LitemallOrder order = orderService.findById(userId, orderId);
        if (order == null) {
            return ResponseUtil.badArgument();
        }
        String name = "voucher_" + orderId + "_";
        List<LitemallStorage> list = storageServiceDb.querySelective(null, name, 1, 100, "add_time", "desc");
        return ResponseUtil.okList(list);
    }

    @PostMapping("/upload")
    public Object upload(@LoginUser Integer userId, @RequestParam("orderId") Integer orderId, @RequestParam("file") MultipartFile file) throws IOException {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        if (orderId == null || orderId <= 0 || file == null || StringUtils.isEmpty(file.getOriginalFilename())) {
            return ResponseUtil.badArgument();
        }
        LitemallOrder order = orderService.findById(userId, orderId);
        if (order == null) {
            return ResponseUtil.badArgument();
        }
        String originalFilename = "voucher_" + orderId + "_" + file.getOriginalFilename();
        LitemallStorage info = storageService.store(file.getInputStream(), file.getSize(), file.getContentType(), originalFilename);
        return ResponseUtil.ok(info);
    }
}
