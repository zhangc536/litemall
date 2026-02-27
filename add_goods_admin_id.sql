-- =============================================
-- 给商品表添加admin_id字段，支持分销商只能看到自己上传的商品
-- 执行前请备份数据库！
-- =============================================

-- 1. 添加admin_id字段到商品表
ALTER TABLE litemall_goods ADD COLUMN admin_id int(11) DEFAULT NULL COMMENT '创建者管理员ID' AFTER deleted;

-- 2. 添加索引
ALTER TABLE litemall_goods ADD INDEX idx_admin_id (admin_id);

-- 3. 更新现有商品的admin_id为管理员（ID=1）
UPDATE litemall_goods SET admin_id = 1 WHERE admin_id IS NULL;

-- 完成
SELECT '商品表添加admin_id字段完成！' as message;
SELECT COUNT(*) as total_goods FROM litemall_goods WHERE deleted = 0;
