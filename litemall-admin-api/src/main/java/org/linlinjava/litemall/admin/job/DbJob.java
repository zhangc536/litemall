package org.linlinjava.litemall.admin.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.db.util.DbUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

/**
 * 数据库定时备份任务
 * 在backup文件夹中备份最近七日的数据库文件
 */
@Component
public class DbJob {
    private final Log logger = LogFactory.getLog(DbJob.class);

    @Autowired
    private Environment environment;

    /*
     * 定时时间是每天凌晨5点。
     */
    @Scheduled(cron = "${litemall.db.backup.cron:0 0 5 * * ?}")
    public void backup() throws IOException {
        logger.info("系统开启定时任务数据库备份");

        String user = environment.getProperty("spring.datasource.druid.username");
        String password = environment.getProperty("spring.datasource.druid.password");
        String url = environment.getProperty("spring.datasource.druid.url");
        int index1 = url.indexOf("3306/");
        int index2 = url.indexOf("?");
        String db = url.substring(index1+5, index2);
        String backupPath = environment.getProperty("litemall.db.backup.path", "backup");
        int keepDays = parseKeepDays(environment.getProperty("litemall.db.backup.keep-days"), 7);

        LocalDate localDate = LocalDate.now();
        String fileName = localDate.toString() + ".sql";
        File file = new File(backupPath, fileName);
        file.getParentFile().mkdirs();
        file.createNewFile();

        // 备份今天数据库
        DbUtil.backup(file, user, password, db);
        // 删除七天前数据库备份文件
        LocalDate before = localDate.minusDays(keepDays);
        String fileBeforeName = before.toString()+".sql";
        File fileBefore = new File(backupPath, fileBeforeName);
        if (fileBefore.exists()) {
            fileBefore.delete();
        }

        logger.info("系统结束定时任务数据库备份");
    }

    private int parseKeepDays(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

}
