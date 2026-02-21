-- 添加用户经验值字段
ALTER TABLE litemall_user ADD COLUMN experience INT DEFAULT 0 COMMENT '经验值';

-- 创建等级配置表
CREATE TABLE IF NOT EXISTS litemall_user_level (
    id TINYINT PRIMARY KEY AUTO_INCREMENT,
    level_name VARCHAR(50) NOT NULL COMMENT '等级名称',
    min_experience INT NOT NULL DEFAULT 0 COMMENT '所需最低经验值',
    icon VARCHAR(255) DEFAULT NULL COMMENT '等级图标',
    description VARCHAR(255) DEFAULT NULL COMMENT '等级描述',
    sort_order TINYINT DEFAULT 0 COMMENT '排序',
    add_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户等级配置表';

-- 插入默认等级配置
INSERT INTO litemall_user_level (id, level_name, min_experience, description, sort_order) VALUES
(1, '普通会员', 0, '注册即可成为普通会员', 1),
(2, '铜牌会员', 100, '累计经验值达到100', 2),
(3, '银牌会员', 500, '累计经验值达到500', 3),
(4, '金牌会员', 2000, '累计经验值达到2000', 4),
(5, '钻石会员', 5000, '累计经验值达到5000', 5);
