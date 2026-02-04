# 供货裂变系统 后端接口文档（REST API 清单）

版本：V1.0
更新时间：2026-02-02

统一约定：
- 接口前缀：/api/v1
- 返回格式：JSON
- 鉴权方式：JWT Token / 微信Session

字段与数据库一致性说明：
- 返回与请求字段需与 [数据库表结构 SQL](./供货裂变系统数据库表结构sql.sql) 完全对齐；任何字段变更需先更新 SQL 并评审，再调整接口与文档。

通用返回格式：
{
  "code": 0,
  "message": "success",
  "data": {}
}

---

## 一、认证与用户模块（M1）

### 接口总览速查（当前实现与联调重点）
- 商品（管理端）：GET/POST/PUT/DELETE /api/v1/admin/product(/:id)
- 订单（管理端）：GET /api/v1/admin/order/list、POST /api/v1/admin/order/{orderId}/audit、POST /api/v1/admin/order/{orderId}/ship、DELETE /api/v1/admin/order/{orderNo}
- 支付凭证（用户端）：POST /api/v1/order/{orderId}/voucher、GET /api/v1/order/{orderId}/vouchers
- 积分（管理端）：GET /api/v1/admin/point/records、DELETE /api/v1/admin/point/record/{id}
- 商品（前台）：GET /api/v1/product/list、GET /api/v1/product/{id}
- 订单（前台）：POST /api/v1/order/create、GET /api/v1/order/list、GET /api/v1/order/{orderId}

说明：开发环境 Mock 与上述管理端接口已联通；生产环境按本文档标准接口接入，响应统一为 { code, message, data }。

### 1. 微信登录
POST /api/v1/auth/wechat/login

请求参数：
{
  "code": "微信登录code",
  "inviteCode": "推荐二维码内容（可选）"
}

返回：
{
  "token": "jwt_token",
  "userInfo": { ... }
}

---

### 2. 获取用户信息
GET /api/v1/user/profile

所需角色：已登录（ADMIN 或 DISTRIBUTOR）

返回：
{
  "id": 1001,
  "nickname": "张三",
  "phone": "138****",
  "referrerId": 1000,
  "status": 1
}

---

### 3. 修改用户信息
PUT /api/v1/user/profile

请求参数：
{
  "nickname": "新昵称",
  "phone": "手机号"
}

---

### 4. 获取我的二维码
GET /api/v1/user/qrcode

返回：
{
  "qrcodeUrl": "https://...",
  "inviteCode": "ABCD1234"
}

---

### 5. 获取我的团队数据
GET /api/v1/user/team

返回：
{
  "directCount": 10,
  "teamCount": 56,
  "teamList": [ ... ]
}

---

## 二、产品与库存模块（M2）

### 6. 获取商品列表
GET /api/v1/product/list

返回：
[
  {
    "id": 1,
    "name": "洗洁精",
    "price": 50,
    "unit": "桶",
    "stock": 10000
  }
]

---

### 7. 获取商品详情
GET /api/v1/product/{id}

---

### 8. 管理员新增商品
POST /api/v1/admin/product

所需角色：ADMIN

请求参数：
{
  "name": "洗洁精",
  "price": 50,
  "unit": "桶",
  "stock": 10000
}

---

### 8.1 管理员获取商品列表
GET /api/v1/admin/product

所需角色：ADMIN

返回：
[
  { "id": 1, "name": "洗洁精", "price": 50, "unit": "桶", "stock": 10000 }
]

---

### 9. 管理员修改商品
PUT /api/v1/admin/product/{id}

所需角色：ADMIN

---

### 9.1 管理员删除商品
DELETE /api/v1/admin/product/{id}

所需角色：ADMIN

---

### 10. 管理员修改库存
PUT /api/v1/admin/inventory/{productId}

所需角色：ADMIN

请求参数：
{
  "totalQuantity": 20000
}

---

## 三、订单与提货模块（M3）

### 11. 创建订单
POST /api/v1/order/create

所需角色：DISTRIBUTOR

请求参数：
{
  "productId": 1,
  "quantity": 200,
  "orderType": "NORMAL | COMMISSION | POINT",
  "addressId": 100
}

返回：
{
  "orderNo": "ORD202602020001",
  "orderId": 5001
}

---

### 12. 获取订单列表
GET /api/v1/order/list

参数：
?status=&page=&size=

---

### 13. 获取订单详情
GET /api/v1/order/{orderId}

---

### 14. 上传支付凭证
POST /api/v1/order/{orderId}/voucher

所需角色：DISTRIBUTOR

请求参数（multipart/form-data）：
file: 图片文件

---

### 15. 管理员审核订单
POST /api/v1/admin/order/{orderId}/audit

所需角色：ADMIN

请求参数：
{
  "status": "APPROVED | REJECTED",
  "remark": "审核意见"
}

---

### 16. 管理员发货
POST /api/v1/admin/order/{orderId}/ship

所需角色：ADMIN

请求参数：
{
  "logisticsCompany": "顺丰",
  "trackingNo": "SF123456"
}

---

### 16.1 订单状态枚举
- WAITING_PROOF：待上传支付凭证（下单后）
- PROOF_UPLOADED：已上传凭证待审核
- APPROVED：审核通过
- REJECTED：审核驳回
- SHIPPED：已发货

---

### 16.2 管理员删除订单
DELETE /api/v1/admin/order/{orderNo}

所需角色：ADMIN

说明：仅用于异常或测试数据清理；生产建议以作废/撤销处理替代硬删除。

---

### 17. 用户确认收货
POST /api/v1/order/{orderId}/confirm-receipt

---

## 四、支付凭证模块（M4）

### 18. 获取订单凭证列表
GET /api/v1/order/{orderId}/vouchers

---

## 五、裂变与佣金模块（M5）

### 19. 获取佣金账户
GET /api/v1/commission/account

返回：
{
  "balanceQuantity": 50,
  "totalEarned": 200,
  "totalUsed": 150
}

---

### 20. 获取佣金流水
GET /api/v1/commission/records

参数：
?page=&size=&type=

---

### 21. 使用佣金提货
POST /api/v1/commission/redeem

请求参数：
{
  "productId": 1,
  "quantity": 20,
  "addressId": 100
}

---

### 22. 管理员佣金审核
POST /api/v1/admin/commission/{recordId}/audit

请求参数：
{
  "status": "APPROVED | REJECTED",
  "remark": "审核说明"
}

---

### 23. 获取裂变关系树（管理员）
GET /api/v1/admin/fission/tree

参数：
?userId=1000

---

## 六、积分系统模块（M6）

### 24. 获取积分账户
GET /api/v1/point/account

---

### 25. 获取积分流水
GET /api/v1/point/records

---

### 26. 使用积分兑换商品
POST /api/v1/point/redeem

请求参数：
{
  "productId": 1,
  "quantity": 20,
  "addressId": 100
}

---

### 27. 管理员发放积分
POST /api/v1/admin/point/grant

请求参数：
{
  "userId": 1001,
  "point": 1000,
  "remark": "活动赠送"
}

---

### 27.1 管理员查询积分记录（管理端）
GET /api/v1/admin/point/records

---

### 27.2 管理员删除积分记录
DELETE /api/v1/admin/point/record/{id}

---

## 七、物流与收货模块（M7）

### 28. 获取我的收货地址列表
GET /api/v1/address/list

---

### 29. 新增收货地址
POST /api/v1/address

请求参数：
{
  "receiverName": "李四",
  "receiverPhone": "1380000",
  "province": "广东省",
  "city": "深圳市",
  "district": "南山区",
  "detailAddress": "科技园XX号",
  "isDefault": true
}

---

### 30. 修改收货地址
PUT /api/v1/address/{id}

---

### 31. 删除收货地址
DELETE /api/v1/address/{id}

---

### 32. 查询物流信息
GET /api/v1/order/{orderId}/logistics

---

## 八、管理后台模块（M8）

### 33. 管理员登录
POST /api/v1/admin/auth/login

---

### 34. 获取用户列表
GET /api/v1/admin/user/list

参数：
?keyword=&status=&page=&size=

---

### 35. 冻结/解冻用户
POST /api/v1/admin/user/{userId}/status

请求参数：
{
  "status": 0 | 1
}

---

### 35.1 创建用户
POST /api/v1/admin/user

请求参数：
{
  "username": "zhangsan",
  "role": "ADMIN | DISTRIBUTOR",
  "phone": "138xxxx"
}

---

### 35.2 删除用户
DELETE /api/v1/admin/user/{userId}

---

### 36. 获取订单列表（管理端）
GET /api/v1/admin/order/list

---

### 37. 获取库存列表
GET /api/v1/admin/inventory/list

---

### 38. 获取财务对账数据
GET /api/v1/admin/finance/list

参数：
?type=&startDate=&endDate=&page=&size=

---

## 九、开发环境 Mock 接口（仅本地调试）

说明：本节接口在前端开发环境由 Vite Dev Server 提供，用于前端独立开发与演示；生产环境请使用上文正式接口。

- GET /api/v1/admin/product
- POST /api/v1/admin/product/create
- POST /api/v1/admin/product/update
- POST /api/v1/admin/product/delete
- GET /api/v1/admin/order/list
- POST /api/v1/admin/order/uploadVoucher
- POST /api/v1/admin/order/audit
- POST /api/v1/admin/order/ship

环境变量：
- 前端开发：VITE_API_BASE_URL=/api/v1
  - 所有请求走本地前端服务器，由 Mock/代理处理
  - 文件位置：admin-web/.env.development


---

### 39. 导出财务报表
GET /api/v1/admin/finance/export

---

## 九、风控与审计模块（M9）

### 40. 获取风控事件列表
GET /api/v1/admin/risk/list

---

### 41. 处理风控事件
POST /api/v1/admin/risk/{eventId}/handle

请求参数：
{
  "status": "RESOLVED | IGNORED",
  "remark": "处理说明"
}

---

### 42. 获取系统操作日志
GET /api/v1/admin/log/list

参数：
?operator=&operation=&startDate=&endDate=&page=&size=

---

## 十、系统配置与通用接口

### 43. 获取系统配置
GET /api/v1/system/config

---

### 44. 修改系统配置（管理员）
POST /api/v1/admin/system/config

---

## 十一、错误码规范（示例）

| code | 含义 |
|------|------|
| 0 | 成功 |
| 1001 | 未登录 |
| 1002 | 无权限 |
| 2001 | 参数错误 |
| 3001 | 库存不足 |
| 3002 | 佣金不足 |
| 3003 | 积分不足 |
| 4001 | 订单状态错误 |
| 5000 | 系统异常 |

---

（本接口文档作为V1.0版本冻结标准，支持Swagger/OpenAPI生成与前后端联调）
