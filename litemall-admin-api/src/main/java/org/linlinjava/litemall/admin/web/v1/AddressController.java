package org.linlinjava.litemall.admin.web.v1;

import org.linlinjava.litemall.admin.util.ResponseV1Util;
import org.linlinjava.litemall.db.domain.LitemallAddress;
import org.linlinjava.litemall.db.service.LitemallAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/address")
@Validated
public class AddressController {
    @Autowired
    private LitemallAddressService addressService;

    @GetMapping("/list")
    public Object list() {
        List<LitemallAddress> addressList = addressService.querySelective(null, null, 1, 50, "add_time", "desc");
        return ResponseV1Util.ok(addressList);
    }
}
