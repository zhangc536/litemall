package org.linlinjava.litemall.admin.web;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.linlinjava.litemall.admin.annotation.RequiresPermissionsDesc;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.db.domain.LitemallUserLevel;
import org.linlinjava.litemall.db.service.LitemallUserLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/userLevel")
@Validated
public class AdminUserLevelController {

    @Autowired
    private LitemallUserLevelService userLevelService;

    @RequiresPermissions("admin:userLevel:list")
    @RequiresPermissionsDesc(menu = {"用户管理", "等级配置"}, button = "查询")
    @GetMapping("/list")
    public Object list() {
        List<LitemallUserLevel> levelList = userLevelService.findAll();
        return ResponseUtil.okList(levelList);
    }

    @RequiresPermissions("admin:userLevel:create")
    @RequiresPermissionsDesc(menu = {"用户管理", "等级配置"}, button = "添加")
    @PostMapping("/create")
    public Object create(@RequestBody LitemallUserLevel level) {
        if (level.getLevelName() == null || level.getMinExperience() == null) {
            return ResponseUtil.badArgument();
        }
        int result = userLevelService.add(level);
        return result > 0 ? ResponseUtil.ok(level) : ResponseUtil.fail(500, "添加失败");
    }

    @RequiresPermissions("admin:userLevel:update")
    @RequiresPermissionsDesc(menu = {"用户管理", "等级配置"}, button = "编辑")
    @PostMapping("/update")
    public Object update(@RequestBody LitemallUserLevel level) {
        if (level.getId() == null) {
            return ResponseUtil.badArgument();
        }
        int result = userLevelService.update(level);
        return result > 0 ? ResponseUtil.ok(level) : ResponseUtil.fail(500, "更新失败");
    }

    @RequiresPermissions("admin:userLevel:delete")
    @RequiresPermissionsDesc(menu = {"用户管理", "等级配置"}, button = "删除")
    @PostMapping("/delete")
    public Object delete(@RequestBody LitemallUserLevel level) {
        if (level.getId() == null) {
            return ResponseUtil.badArgument();
        }
        int result = userLevelService.delete(level.getId());
        return result > 0 ? ResponseUtil.ok() : ResponseUtil.fail(500, "删除失败");
    }
}
