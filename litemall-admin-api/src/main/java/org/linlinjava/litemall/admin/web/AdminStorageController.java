package org.linlinjava.litemall.admin.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.linlinjava.litemall.admin.annotation.RequiresPermissionsDesc;
import org.linlinjava.litemall.core.storage.StorageService;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.core.validator.Order;
import org.linlinjava.litemall.core.validator.Sort;
import org.linlinjava.litemall.db.domain.LitemallStorage;
import org.linlinjava.litemall.db.service.LitemallStorageService;
import org.linlinjava.litemall.db.util.DbUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/storage")
@Validated
public class AdminStorageController {
    private final Log logger = LogFactory.getLog(AdminStorageController.class);

    @Autowired
    private StorageService storageService;
    @Autowired
    private LitemallStorageService litemallStorageService;
    @Autowired
    private Environment environment;

    @RequiresPermissions("admin:storage:list")
    @RequiresPermissionsDesc(menu = {"系统管理", "对象存储"}, button = "查询")
    @GetMapping("/list")
    public Object list(String key, String name,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {
        List<LitemallStorage> storageList = litemallStorageService.querySelective(key, name, page, limit, sort, order);
        return ResponseUtil.okList(storageList);
    }

    @RequiresPermissions("admin:storage:create")
    @RequiresPermissionsDesc(menu = {"系统管理", "对象存储"}, button = "上传")
    @PostMapping("/create")
    public Object create(@RequestParam("file") MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        LitemallStorage litemallStorage = storageService.store(file.getInputStream(), file.getSize(),
                file.getContentType(), originalFilename);
        return ResponseUtil.ok(litemallStorage);
    }

    @RequiresPermissions("admin:storage:read")
    @RequiresPermissionsDesc(menu = {"系统管理", "对象存储"}, button = "详情")
    @PostMapping("/read")
    public Object read(@NotNull Integer id) {
        LitemallStorage storageInfo = litemallStorageService.findById(id);
        if (storageInfo == null) {
            return ResponseUtil.badArgumentValue();
        }
        return ResponseUtil.ok(storageInfo);
    }

    @RequiresPermissions("admin:storage:update")
    @RequiresPermissionsDesc(menu = {"系统管理", "对象存储"}, button = "编辑")
    @PostMapping("/update")
    public Object update(@RequestBody LitemallStorage litemallStorage) {
        if (litemallStorageService.update(litemallStorage) == 0) {
            return ResponseUtil.updatedDataFailed();
        }
        return ResponseUtil.ok(litemallStorage);
    }

    @RequiresPermissions("admin:storage:delete")
    @RequiresPermissionsDesc(menu = {"系统管理", "对象存储"}, button = "删除")
    @PostMapping("/delete")
    public Object delete(@RequestBody LitemallStorage litemallStorage) {
        String key = litemallStorage.getKey();
        if (StringUtils.isEmpty(key)) {
            return ResponseUtil.badArgument();
        }
        litemallStorageService.deleteByKey(key);
        storageService.delete(key);
        return ResponseUtil.ok();
    }

    @RequiresPermissions("admin:storage:backup")
    @RequiresPermissionsDesc(menu = {"系统管理", "数据库备份"}, button = "备份")
    @PostMapping("/backup")
    public Object backup() throws IOException {
        String user = environment.getProperty("spring.datasource.druid.username");
        String password = environment.getProperty("spring.datasource.druid.password");
        String url = environment.getProperty("spring.datasource.druid.url");
        String db = extractDbName(url);
        if (user == null || password == null || db == null) {
            return ResponseUtil.fail(502, "数据库配置缺失");
        }
        LocalDateTime now = LocalDateTime.now();
        String fileName = db + "-" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".sql";
        File dir = new File("backup");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        DbUtil.backup(file, user, password, db);
        Map<String, Object> data = new HashMap<>();
        data.put("name", fileName);
        data.put("size", file.length());
        data.put("path", file.getAbsolutePath());
        data.put("time", now.toString());
        return ResponseUtil.ok(data);
    }

    @RequiresPermissions("admin:storage:backupList")
    @RequiresPermissionsDesc(menu = {"系统管理", "数据库备份"}, button = "列表")
    @GetMapping("/backup/list")
    public Object backupList() {
        File dir = new File("backup");
        List<Map<String, Object>> list = new ArrayList<>();
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((file, name) -> name.endsWith(".sql"));
            if (files != null) {
                Arrays.sort(files, (left, right) -> Long.compare(right.lastModified(), left.lastModified()));
                for (File file : files) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", file.getName());
                    item.put("size", file.length());
                    item.put("path", file.getAbsolutePath());
                    item.put("time", new Date(file.lastModified()));
                    list.add(item);
                }
            }
        }
        return ResponseUtil.okList(list);
    }

    private String extractDbName(String url) {
        if (url == null) {
            return null;
        }
        int slashIndex = url.lastIndexOf("/");
        if (slashIndex < 0) {
            return null;
        }
        int questionIndex = url.indexOf("?", slashIndex);
        if (questionIndex < 0) {
            return url.substring(slashIndex + 1);
        }
        return url.substring(slashIndex + 1, questionIndex);
    }
}
