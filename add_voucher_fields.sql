-- 添加支付凭证相关字段到订单表
ALTER TABLE litemall_order ADD COLUMN pay_voucher VARCHAR(512) DEFAULT NULL COMMENT '支付凭证图片URL';
ALTER TABLE litemall_order ADD COLUMN voucher_status SMALLINT DEFAULT NULL COMMENT '凭证状态: 0-待审核, 1-已通过, 2-已拒绝';
