INSERT INTO litemall_permission (id, role_id, permission, create_time, update_time, deleted) 
VALUES (35, 2, 'admin:order:delete', NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE permission = 'admin:order:delete';
