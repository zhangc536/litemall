-- =============================================
-- 权限系统重构：只保留管理员和分销商两个角色
-- 执行前请备份数据库！
-- =============================================

-- 1. 清空现有权限数据
DELETE FROM litemall_permission;

-- 2. 更新角色表：只保留管理员和分销商
DELETE FROM litemall_role;
INSERT INTO litemall_role (id, name, `desc`, enabled, add_time, update_time, deleted) VALUES
(1, '管理员', '系统管理员，拥有所有权限', 1, NOW(), NOW(), 0),
(2, '分销商', '分销商，拥有商品管理和订单管理权限', 1, NOW(), NOW(), 0);

-- 3. 插入管理员权限（角色ID=1，拥有所有权限）
INSERT INTO litemall_permission (role_id, permission, add_time, update_time, deleted) VALUES
(1, '*', NOW(), NOW(), 0);

-- 4. 插入分销商权限（角色ID=2）
-- 商品分类权限（查看、创建、编辑）
INSERT INTO litemall_permission (role_id, permission, add_time, update_time, deleted) VALUES
(2, 'admin:category:list', NOW(), NOW(), 0),
(2, 'admin:category:read', NOW(), NOW(), 0),
(2, 'admin:category:create', NOW(), NOW(), 0),
(2, 'admin:category:update', NOW(), NOW(), 0);

-- 商品品牌权限（查看、创建、编辑）
INSERT INTO litemall_permission (role_id, permission, add_time, update_time, deleted) VALUES
(2, 'admin:brand:list', NOW(), NOW(), 0),
(2, 'admin:brand:read', NOW(), NOW(), 0),
(2, 'admin:brand:create', NOW(), NOW(), 0),
(2, 'admin:brand:update', NOW(), NOW(), 0);

-- 商品管理权限（查看、创建、编辑）
INSERT INTO litemall_permission (role_id, permission, add_time, update_time, deleted) VALUES
(2, 'admin:goods:list', NOW(), NOW(), 0),
(2, 'admin:goods:read', NOW(), NOW(), 0),
(2, 'admin:goods:create', NOW(), NOW(), 0),
(2, 'admin:goods:update', NOW(), NOW(), 0);

-- 订单管理权限（查看、审核、发货、删除）
INSERT INTO litemall_permission (role_id, permission, add_time, update_time, deleted) VALUES
(2, 'admin:order:list', NOW(), NOW(), 0),
(2, 'admin:order:audit', NOW(), NOW(), 0),
(2, 'admin:order:ship', NOW(), NOW(), 0),
(2, 'admin:order:delete', NOW(), NOW(), 0);

-- 积分管理权限（查看、编辑）
INSERT INTO litemall_permission (role_id, permission, add_time, update_time, deleted) VALUES
(2, 'admin:user:points', NOW(), NOW(), 0),
(2, 'admin:user:points:list', NOW(), NOW(), 0),
(2, 'admin:user:points:read', NOW(), NOW(), 0),
(2, 'admin:user:points:update', NOW(), NOW(), 0);

-- 积分商品权限（查看、创建、编辑）
INSERT INTO litemall_permission (role_id, permission, add_time, update_time, deleted) VALUES
(2, 'admin:pointgoods:list', NOW(), NOW(), 0),
(2, 'admin:pointgoods:read', NOW(), NOW(), 0),
(2, 'admin:pointgoods:create', NOW(), NOW(), 0),
(2, 'admin:pointgoods:update', NOW(), NOW(), 0);

-- 统计查看权限
INSERT INTO litemall_permission (role_id, permission, add_time, update_time, deleted) VALUES
(2, 'admin:stat:user', NOW(), NOW(), 0),
(2, 'admin:stat:order', NOW(), NOW(), 0),
(2, 'admin:stat:goods', NOW(), NOW(), 0);

-- 文件上传权限
INSERT INTO litemall_permission (role_id, permission, add_time, update_time, deleted) VALUES
(2, 'admin:storage:list', NOW(), NOW(), 0),
(2, 'admin:storage:create', NOW(), NOW(), 0),
(2, 'admin:storage:read', NOW(), NOW(), 0);

-- 5. 更新现有管理员账号的角色为管理员
UPDATE litemall_admin SET role_ids = '[1]' WHERE deleted = 0;

-- 完成
SELECT '权限重构完成！' as message;
SELECT * FROM litemall_role WHERE deleted = 0;
SELECT COUNT(*) as permission_count FROM litemall_permission WHERE deleted = 0;
