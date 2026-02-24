-- 积分兑换系统重构数据库变更
-- 1. 添加订单类型字段
ALTER TABLE litemall_order ADD COLUMN order_type TINYINT DEFAULT 0 COMMENT '订单类型：0-普通订单，1-积分订单';

-- 2. 添加积分消耗字段（用于积分订单）
ALTER TABLE litemall_order ADD COLUMN points_used INT DEFAULT 0 COMMENT '消耗积分数量';

-- 3. 创建积分流水表
CREATE TABLE IF NOT EXISTS litemall_points_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT '用户ID',
    points INT NOT NULL COMMENT '积分变动数量（正数增加，负数减少）',
    type TINYINT NOT NULL COMMENT '类型：1-订单获得，2-积分兑换，3-管理员调整，4-订单取消返还，5-审核拒绝返还',
    order_id INT DEFAULT NULL COMMENT '关联订单ID',
    order_sn VARCHAR(63) DEFAULT NULL COMMENT '关联订单编号',
    description VARCHAR(255) DEFAULT NULL COMMENT '描述',
    balance_after INT DEFAULT 0 COMMENT '变动后余额',
    add_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id),
    INDEX idx_add_time (add_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水记录表';

-- 4. 更新现有积分订单的订单类型
UPDATE litemall_order SET order_type = 1, points_used = integral_price 
WHERE pay_voucher LIKE '积分兑换%' AND deleted = 0;

-- 5. 积分商品表（如果不存在则创建）
CREATE TABLE IF NOT EXISTS litemall_point_goods (
    id INT PRIMARY KEY AUTO_INCREMENT,
    goods_id INT NOT NULL COMMENT '关联商品ID',
    goods_name VARCHAR(127) NOT NULL COMMENT '商品名称',
    goods_brief VARCHAR(255) DEFAULT NULL COMMENT '商品简介',
    pic_url VARCHAR(255) DEFAULT NULL COMMENT '图片URL',
    points INT NOT NULL DEFAULT 0 COMMENT '所需积分',
    price DECIMAL(10,2) DEFAULT 0.00 COMMENT '所需金额（备用）',
    amount INT DEFAULT 0 COMMENT '库存数量',
    status TINYINT DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    sort_order INT DEFAULT 0 COMMENT '排序',
    add_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_goods_id (goods_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分商品表';
