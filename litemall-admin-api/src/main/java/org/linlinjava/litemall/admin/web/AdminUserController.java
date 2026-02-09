package org.linlinjava.litemall.admin.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.linlinjava.litemall.admin.annotation.RequiresPermissionsDesc;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.core.validator.Order;
import org.linlinjava.litemall.core.validator.Sort;
import org.linlinjava.litemall.db.domain.LitemallUser;
import org.linlinjava.litemall.db.service.LitemallUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/admin/user")
@Validated
public class AdminUserController {
    private final Log logger = LogFactory.getLog(AdminUserController.class);

    @Autowired
    private LitemallUserService userService;

    @RequiresPermissions("admin:user:list")
    @RequiresPermissionsDesc(menu = {"用户管理", "会员管理"}, button = "查询")
    @GetMapping("/list")
    public Object list(String username, String mobile,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {
        List<LitemallUser> userList = userService.querySelective(username, mobile, page, limit, sort, order);
        Set<Integer> inviterIds = new HashSet<>();
        for (LitemallUser user : userList) {
            Integer inviterUserId = user.getInviterUserId();
            if (inviterUserId != null && inviterUserId > 0) {
                inviterIds.add(inviterUserId);
            }
        }
        Map<Integer, LitemallUser> inviterMap = new HashMap<>();
        if (!inviterIds.isEmpty()) {
            List<LitemallUser> inviterList = userService.queryByIds(new ArrayList<>(inviterIds));
            for (LitemallUser inviter : inviterList) {
                inviterMap.put(inviter.getId(), inviter);
            }
        }
        List<Map<String, Object>> list = new ArrayList<>(userList.size());
        for (LitemallUser user : userList) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", user.getId());
            item.put("username", user.getUsername());
            item.put("nickname", user.getNickname());
            item.put("avatar", user.getAvatar());
            item.put("mobile", user.getMobile());
            item.put("userLevel", user.getUserLevel());
            item.put("status", user.getStatus());
            item.put("inviterUserId", user.getInviterUserId());
            LitemallUser inviter = inviterMap.get(user.getInviterUserId());
            if (inviter != null) {
                item.put("inviterName", inviter.getNickname());
            }
            list.add(item);
        }
        return ResponseUtil.okList(list, userList);
    }

    @RequiresPermissions("admin:user:delete")
    @RequiresPermissionsDesc(menu = {"用户管理", "会员管理"}, button = "删除")
    @PostMapping("/delete")
    public Object delete(@RequestBody LitemallUser user) {
        Integer id = user.getId();
        if (id == null) {
            return ResponseUtil.badArgument();
        }
        userService.deleteById(id);
        return ResponseUtil.ok();
    }
}
